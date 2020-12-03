# otpsmart-cli



Command line tool to get accounts list and statements in XLSX and JSON formats.

### Build

```bash
./gradlew jar
```

### Usage

#### Getting help

```bash
$ java -jar ./otpsmart-cli-0.1.jar -h

Usage: otpsmart-cli options_list
Subcommands: 
    accounts - List card accounts
    statement - Fetch account statement

Options: 
    --sessionId, -s -> Existing sessionId to skip authorization flow { String }
    --userId, -u -> OTPSmart contract id (login). Could be omitted if sessionId provided { String }
    --password, -p -> OTPSmart raw password. Could be omitted if sessionId or passwordHash provided { String }
    --passwordHash, -ph -> Password hashed with MD5 algorithm { String }
    --json, -j [false] -> Output as JSON 
    --help, -h -> Usage info 

```

#### Getting accounts list

```bash
$ java -jar ./otpsmart-cli-0.1.jar accounts -u <login> -p <raw password> --json

sessionId=339B4E7873F89D6F2F9EED41EAD81700
SMS token: 281647
UA4630052800000002620806xxxxx 546886******0955 xx,xxx.xx UAH
UA4630052800000002620806xxxxx 546886******5758 xx,xxx.xx UAH
UA6130052800000002620501xxxxx 510094******8228 xx,xxx.xx UAH
2625703102xxxx                516887******3221 xx,xxx.xx UAH
2625703102xxxx                516887******2746 xx,xxx.xx UAH
UA4730052800000002620702xxxxx 533194******4745 xx,xxx.xx UAH
UA4730052800000002620702xxxxx 533194******3602 xx,xxx.xx UAH
UA4730052800000002620702xxxxx 533194******6017 xx,xxx.xx UAH
UA4730052800000002620702xxxxx 533194******9699 xx,xxx.xx UAH

```

Once you have active **sessionId**, login credentials could be replaced with **--sessionId** / **-s** argument.

```bash
java -jar ./otpsmart-cli-0.1.jar accounts -s 339B4E7873F89D6F2F9EED41EAD81700 --json
JSON file stored: /Projects/otpclient/accounts.json
[
    {
    ...
		},
		...
]
```

#### Fetch statements

```bash
java -jar ./otpsmart-cli-0.1.jar statement *0955 01.11.2020 03.12.2020 -s 339B4E7873F89D6F2F9EED41EAD81700 --json

XLSX file stored: /Projects/otpclient/UA4630052800000002620806xxxxx_20201101_20201203.xls
JSON file stored: /Projects/otpclient/UA4630052800000002620806xxxxx_20201101_20201203.json
```

