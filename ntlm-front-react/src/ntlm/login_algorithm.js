import * as Ntlm from "./ntlm";

/**
 * Взывает методы формирования NTLM сообщений и производит обмен с бэком.
 * Обмен происходит с помощью 2 HTTP запрососв.
 * Нет проверок на негативные ответы сервера.
 *
 * @param hostname не важно
 * @param domain Active Directory домен, в котором проезвести проверку логина-пароля
 * @param login
 * @param password
 */
export function doAllActionsForNTLM(hostname, domain, login, password) {
    let ntlmMessage = Ntlm.createMessage1(hostname, domain);
    window.fetch('/api/login', {
        credentials: 'same-origin',
        method: 'POST',
        body: 'NTLM ' + ntlmMessage.toBase64()
    })
        .then(response => {
            return response.text();
        })
        .then(serverChallenge => {
            const challenge = Ntlm.getChallenge(serverChallenge);
            let type3Message = Ntlm.createMessage3(challenge, domain, login, password, hostname);
            return window.fetch('/api/login', {
                credentials: 'same-origin',
                method: 'POST',
                body: 'NTLM ' + type3Message.toBase64()
            })
        })
        .then(response => {
            return response.text();
        })
        .then(result => {
            console.log('response: ', result);
        })
        .catch(error => console.log('error: ', error));
}
