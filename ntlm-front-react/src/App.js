import './App.css';
import * as React from "react";
import {doAllActionsForNTLM} from "./ntlm/login_algorithm";

class App extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            login: "",
            password: "",
            domain: "MYDOMAIN"
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
        doAllActionsForNTLM(hostname, domain, login, password);
    }
}

export default App;
