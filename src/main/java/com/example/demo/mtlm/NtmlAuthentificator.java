package com.example.demo.mtlm;

import jcifs.util.Base64;
import org.ntlmv2.liferay.*;
import org.ntlmv2.liferay.util.HttpHeaders;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.*;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;

@Component
public class NtmlAuthentificator {

    private static final Logger log = LoggerFactory.getLogger(NtmlAuthentificator.class);

    /** Random number generator for challenge creation. */
    private final SecureRandom secureRandom = new SecureRandom();

    private NtlmManager ntlmManager;

    private final Map<String, Object> cache = new HashMap<>();

    @Value("ntlm.domain.name")
    private String controllerDomain;

    @Value("ntlm.domain.controller.address")
    private String domainControllerAddress;

    @Value("ntlm.domain.controller.hostname")
    private String domainControllerHostName;

    @Value("ntlm.service.account")
    private String serviceAccount;

    @Value("ntlm.service.password")
    private String servicePassword;

    public NtmlAuthentificator() {
    }

    @PostConstruct
    public void init() {
        this.ntlmManager = new NtlmManager(
                controllerDomain,
                domainControllerAddress,
                domainControllerHostName,
                serviceAccount,
                servicePassword);
    }

    public NtlmState auth(String ntlmMessage, String clientSessionId) throws IOException {
        if (ntlmMessage != null && ntlmMessage.startsWith("NTLM")) {
            byte[] ntlmBinMessage = Base64.decode(ntlmMessage.substring(5));
            byte ntlmMessageType = ntlmBinMessage[8];
            if (ntlmMessageType == 1) {
                log.debug("Create server challenge...");
                byte[] serverChallenge = new byte[8];
                secureRandom.nextBytes(serverChallenge);
                byte[] serverChallengeMessage = ntlmManager.negotiate(
                        ntlmBinMessage, serverChallenge);
                synchronized (cache) {
                    cache.put(clientSessionId, serverChallenge);
                }
                return new NtlmState(serverChallengeMessage, null, NtlmState.NtlmServerStage.SEND_SERVER_CHALLENGE);
            } else if (ntlmMessageType == 3) {
                byte[] serverChallenge = null;
                synchronized (cache) {
                    serverChallenge = (byte[]) cache.get(clientSessionId);
                }
                if (serverChallenge == null) {
                    return new NtlmState(null, null, NtlmState.NtlmServerStage.REQUEST_CLIENT_CHALLENGE);
                } else {
                    NtlmUserAccount ntlmUserAccount = null;
                    try {
                        log.debug("Try authenticating user now...");
                        ntlmUserAccount = ntlmManager.authenticate(ntlmBinMessage, serverChallenge);
                        log.info("Authentication was successful. Creating session.");
                    } catch (Exception e) {
                        log.error("NTLM authentication failed: ", e);
                        return new NtlmState(null, null, NtlmState.NtlmServerStage.AUTH_FAIL);
                    } finally {
                        synchronized (cache) {
                            cache.remove(clientSessionId);
                        }
                    }
                    if (ntlmUserAccount == null) {
                        return new NtlmState(null, null, NtlmState.NtlmServerStage.AUTH_FAIL);
                    }
                }
            } else {
                throw new IllegalArgumentException("Не удалось обработать тип сообщения: " + ntlmMessageType);
            }
        } else {
            return new NtlmState(null, null, NtlmState.NtlmServerStage.NOT_NTLM);
        }
        return new NtlmState(null, null, NtlmState.NtlmServerStage.AUTH_FAIL);
    }

    private void sendWwwAuthenticateResponse(HttpServletResponse response)
            throws IOException {
        response.setContentLength(0);
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "NTLM");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.flushBuffer();
    }

    public static class NtlmState {

        private final byte[] serverChallenge;

        private final NtlmUserAccount userAccount;

        private final NtlmServerStage stage;

        public NtlmState(byte[] serverChallenge, NtlmUserAccount userAccount, NtlmServerStage stage) {
            this.serverChallenge = serverChallenge;
            this.userAccount = userAccount;
            this.stage = stage;
        }

        public enum NtlmServerStage {
            REQUEST_CLIENT_CHALLENGE,
            SEND_SERVER_CHALLENGE,
            AUTH_OK,
            AUTH_FAIL,
            NOT_NTLM
        }

        public byte[] getServerChallenge() {
            return serverChallenge;
        }

        public NtlmUserAccount getUserAccount() {
            return userAccount;
        }

        public NtlmServerStage getStage() {
            return stage;
        }
    }
}
