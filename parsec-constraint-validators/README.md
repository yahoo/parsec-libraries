# Parsec Constraint Validators

The constraint validators library contains a list of useful annotations that can be used when building your web application.

## Dependency
```
repositories {
    mavenCentral()
}
dependencies {
    compile group: "com.yahoo.parsec", name: "parsec-constraint-validators", version: ''
}
```

## Usage

Import the package via:
```
import com.yahoo.parsec.constraint.validators.*
```

or you can import the annotation individually as below:

- @CountryCode
- @DateTime
- @IpAddress
- @LanguageTag
- @LatLong
- @Msisdn
- @MsisdnComm
- @ValidCurrency
- @ValidTimeZone
