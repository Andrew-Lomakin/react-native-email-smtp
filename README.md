# React native email smtp
android only

- send smtp message
- attach xml or json file 

Collected from sticks and stones.
I'm not a java developer. For myself.
Got push is welcome.

```javascript
import MailSMTP from 'react-native-mail-smtp';

MailSMTP.sendMail({
    mailhost: smtp.google.com
    port: '465',
    username: 'username',
    password: 'password',
    from: 'from',
    to: 'to',
    subject: 'subject',
    htmlBody: 'body',
    attachment: true,
    format: 'JSON'
})
```