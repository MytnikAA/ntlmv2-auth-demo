import './App.css';
import * as React from "react";
import * as Ntlm from './ntlm';

class App extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            login: "",
            password: "",
            domain: "avt"
        }
    }

    render() {
        return (
            <div className="App">
                <div id="login-form">
                    <label>
                        Логин:
                    </label>
                    <input value={this.state.login} onChange={this.updateLogin.bind(this)}
                           placeholder="Введите доменный логин"/>
                    <label>
                        Пароль:
                    </label>
                    <input value={this.state.password} onChange={this.updatePassword.bind(this)}
                           placeholder="Введите доменный пароль"/>
                    <button id="login-btn" onClick={this.doLogin.bind(this)}>Войти</button>
                </div>
            </div>
        );
    }

    updateLogin(event) {
        this.setState({
            login: event.target.value
        })
    }

    updatePassword(event) {
        this.setState({
            password: event.target.value
        })
    }

    doLogin() {
        const domain = this.state.domain;
        const login = this.state.login;
        const password = this.state.password;
        const hostname = "client-pc";
        console.log(login + " " + password);
        let ntlmMessage = Ntlm.createMessage1(hostname, domain);
        console.log('ntlm message: ', ntlmMessage);
        window.fetch('/api/login', {
            credentials: 'same-origin',
            method: 'POST',
            body: 'NTLM ' + ntlmMessage.toBase64()
        })
            .then(response => {
                console.log('response: ', response);
                return response.text();
            })
            .then(serverChallenge => {
                console.log('serverChallenge: ', serverChallenge);
                const challenge = Ntlm.getChallenge(serverChallenge);
                let type3Message = Ntlm.createMessage3(challenge, domain, login, password, hostname);
                console.log('type3Message: ', type3Message.toBase64())
                return window.fetch('/api/login', {
                    credentials: 'same-origin',
                    method: 'POST',
                    body: 'NTLM ' + type3Message.toBase64()
                })
            })
            .then(response => {
                console.log('response: ', response);
                return response.text();
            })
            .then(result => {
                console.log('response: ', result);
            })
            .catch(error => console.log('error: ', error));
    }
}

export default App;
