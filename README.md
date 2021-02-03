# ntlmv2-auth-demo


1. Create `computer account` in AD controller:
Open MMC console, in MYDOMAIN -> Computers: New -> Computer: my_comp_account (without $)

2. Set passwort for my_comp_account. Copy file SetComputerPassword.vbs (from repo root), open cmd as admin, run script:
``` cmd
cscript SetComputerPassword.vbs my_comp_account$@MYDOMAIN my_comp_account_password
```

!!! There will be no magic without $ symbol !!!

3. Create file application.properties in workdir with:
``` properties
ntlm.domain.name=MYDOMAIN
ntlm.domain.controller.address=AD-HOST-OR-IP
ntlm.domain.controller.hostname=AD_NAME
ntlm.service.account=my_comp_account$@MYDOMAIN
ntlm.service.password=my_comp_account_password
```

Links:
https://issues.liferay.com/browse/LPS-11391
