# Demo NTLM authentication. Clientside.

Клиентская часть аутентификации через NTLM.

Пример показывает ввод логина и пароля через кастомную форму.
Алгоритмы формирования NTLM сообщений полностью реализованы на js (взято из либы и подпилено).

src/ntlm/ntlm.js - алгоритмы формирования NTLM сообщений из https://github.com/erlandranvinge/ntlm.js
src/ntlm/login_algorithm.js

