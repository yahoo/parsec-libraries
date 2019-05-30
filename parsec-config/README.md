# Parsec Config
The Parsec Config library lets you set configurations based on different environment, or profile, such as alpha, beta or production.
It allows you to introduce a set of standardized/best practice configuration definition for your Java web application.
You can easily understand how to use this library by following the implementation steps below

### Construct your configurations (.conf files)
+ Start by defining shared and environment-specific settings
+ place your configurations files under <projectDir>/src/main/resources/, so the library can find them
+ Configuration examples as below:

*common_default.conf* (shared settings):
```
simpleKey = defaultValue
common {
    errorMsg = this is an error
}
```

*dev.example.conf* (environment-specific):
```
include "common_default.conf"

simpleKey = overrideValue
number = 123
duration = 10m
boolean = true

db {
    driver = com.mysql.jdbc.Driver
    username = testuser
    keyname = aabb
}

booleanList: [ true, false, true ]
numberList: [ 1, 2, 3, 4, 5, 6 ]
stringList: [ "abc", "456", "xyz" ]
durationList: [ 10m, 300s ]

configList: [
    {
        key1 = val1
    }
    {
        key2 = val2
    }
]
```

### Include dependency
Note that if your project inherits from Parsec Base Build, it is already added for you.

```
repositories {
    mavenCentral()
}

dependencies {
    compile group: 'com.yahoo.parsec', name: 'parsec-config', version: ''
}
```

### Use Parsec Config in your code
```java
public class ExampleConfig {
  static final ParsecConfig CONFIG = ParsecConfigFactory.load();

  public String getDbUserName() {
    return CONFIG.getString("db.username");
  }

  public String getErrorMessage() {
    return CONFIG.getString("common.errorMsg");
  }
}
```

### Setup environment settings for different environments
The library uses *parsec.conf.env.context* system property key to identify the environment. In other words, specifying
 `-Dparsec.conf.env.context=test.conf` in JVM command line argument would cause the library to read *test.conf* in resources.

 To configure the environment in Gradle's unit testing, using the systemProperty() provided by task type: Test.
 ```
    tasks.withType(Test){
        //...
        systemProperty "parsec.conf.env.context", "test.conf"
    }
 ```
This will essentially pass in the system property to the testing-used JVM, for all Test tasks.


