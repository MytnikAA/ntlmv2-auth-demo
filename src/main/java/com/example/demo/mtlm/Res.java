package com.example.demo.mtlm;

import org.ntlmv2.liferay.NtlmUserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.Base64;

@RestController
@RequestMapping("/api")
public class Res {

    @Autowired
    private NtmlAuthentificator ntmlAuthentificator;

    @GetMapping()
    public String ping() {
        return "pong";
    }

    @PostMapping("login")
    @ResponseBody
    public String login(HttpSession session, @RequestBody String ntlmMessage) {
        NtmlAuthentificator.NtlmState auth = null;
        try {
            auth = ntmlAuthentificator.auth(ntlmMessage, session.getId());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (auth != null) {
            if (auth.getServerChallenge() != null) {
                return "NTLM " + Base64.getEncoder().encodeToString(auth.getServerChallenge());
            }
            if (auth.getUserAccount() != null) {
                return auth.getUserAccount().getUserName();
            }
            return "";
        } else {
            return "";
        }
    }
}
