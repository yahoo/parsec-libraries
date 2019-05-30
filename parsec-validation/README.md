# Parsec Validation (Input Validation)

+ Define standard method for input validation in Parsec Java web applications
+ Support web applications' requirement to validate user input depending on their business logic

### Design and Implementation
+ Adopt [bean validation in Jersey](https://jersey.java.net/documentation/latest/bean-validation.html), which is Jersey framework's native feature
+ Jersey bean validation depends directly on Hibernate Validator. It supports validating all forms of input from Jersey framework such as @PathParam, @QueryParam, @FormParam, and request body json object by declaring hibernate validation annotations (such as @Size, @Null, @NotNull, ...)
+ User can also customize their validation exception {code}, {message} by defining server properties in DefaultWebListner.java
+ Implemented *ParsecValidationAutoDiscoverable.java* to register higher priority for ParsecConstraintViolationExceptionMapper (because the default priority of Jersey's ConstraintViolationExceptionMapper for
ConstraintViolationException is higher than others)
+ Integrate hibernate validation annotation with Parsec RDL Generator, which you can define hibernate validation annotation in the rdl schema (.rdl) and the annotations will be generated

### Package
+ Requires jersey-bean-validation dependency
+ Parsec Validation is added to the dependency by default if the project inheritis from Parsec Base Build.
+ When Parsec Validation library is added to the dependencies, validation feature will be enabled automatically without requiring any additional function calls.
+ Implemented ParsecValidationExceptionMapper to catch ConstraintViolationException (which is thrown by Hibernate Validator) and the error layout is reformmated to follow TC Rest API Convention

### Dependency
```
repositories {
    mavenCentral()
}

dependencies {
    compile group:'com.yahoo.parsec', name: 'parsec-validation', version: ''
}
```

### Error Layout Example
```
{
    "error":{
        "detail":[
            {
                "message":"may not be null",
                "messageTemplate":"{javax.validation.constraints.NotNull.message}",
                "path":"SampleResources.postUser.user.id",
                "invalidValue":null
            },
            {
                "message":"'invalid_name' max 5",
                "messageTemplate":"'${validatedValue}' max {max}",
                "path":"SampleResources.postUser.user.name",
                "invalidValue":"invalid_name"
            }
        ],
        "code":40001,
        "message":"constraint violation validate error unittest"
    }
}
```

### Supported Validation Constraints

| constraint | supported RDL data type |  Use |
|:-----------|:------------------------|:-----|
| x_min=”x”	              | Int8, Int16, Int32, Int64, Byte	| value should be greater or equals x |
| x_max=”x”	              | Int8, Int16, Int32, Int64, Byte	| value should be less or equals x |
| x_size=”min=x,max=y”	  | String, Array, Map              | value should be between x and y (inclusive) |
| x_pattern=”regexp=”x”“ |	String	                        | value should match the regex defined by x |
| x_must_validate	 | Struct	| Performs validation recursively on the associated object |
| x_name=”x”  |	String	     | use x instead if the originl rdl name to get input value |
| x_not_null  |	any type	 | value should be not null |
| x_not_blank |	String	     | value should be not null and size is greater than zero |
| x_not_empty |	Array, Map	 | the size of the value must be greater than 0 and is not null |
| x_country_code	| String	| ISO 639 country code, in lower case |
| x_currency	    | String	| ISO 4217 currency |
| x_language_tag    |	String	| BCP 47 language tag |
| x_null            |	any type	| value should be null |
| x_digits=”integer=x,fraction=y”	| float32, float64	| value should match x in integer part, and also match y in fraction part |

### Using Validation Groups
Validation groups allows you to control the set of constraints to enable per object for an endpoint.
Please note that only data object validation supports this feature.
The syntax to define constraint validation groups follows this syntax: <constraint>=”groups=<groups> [, <other settings>]”. Where:

+ <constraint> is one of the constraint in the previous section
+ <groups> is a | seperated list of groups that will enable this constraint.
+ <other settings> are other settings supported by the contraint

For example, this size constraint will only be enabled if group is create or update:
```java
String someField (x_size="min=3, max=5, groups=create|update");
```
To control which validation group to enable for an object in a particular endpoint,
use x_must_validate=<group name> syntax. Please be advised that only one validation group may be defined per object at a time. For example:
```java
Object someObject (x_must_validate="update");
```
**Please note that the defined validation group must exist (i.e. used in an object), otherwise you may receive Java compile time errors.

### Customizing Validation Error Code and Message
Customize validation error {code}, {message} in *DefaultWebListener.java*:
```java
@WebListener
public class DefaultWebListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        // add api application servlet with customized validation error code, message
        final DefaultApplication app = new DefaultApplication();
        app.property( ParsecValidationExceptionMapper.PROP_VALIDATION_DEFAULT_ERROR_CODE, 40001);
        app.property( ParsecValidationExceptionMapper.PROP_VALIDATION_DEFAULT_ERROR_MSG, "test validation error message");
        ...
     }
}
```

### Example of Adding Validation in RDL
+ Adding validation to your name field in User struct at src/main/rdl/sample.rdl - Here we set the limitation to User.name.
We define the length of the name to be >= 3 && <=5
```
...
type User struct {
    string name (x_size="min=3,max=5");
    int32 age;
}
...
```
+ Adding validation to POST data
```
...
resource string POST "/user" {
    User user (x_must_validate);
...
```
+ Then we can re-generate the java code and run the web server. *Note that jettyRun has dependency on parsec-generate, so technically jettyRun itself is sufficient.*
```
$ gradle parsec-generate
```
```
$ gradle jettyRun
```
+ Check if the validation works:
```
$ curl -H 'Content-Type: application/json' -d '{"name":"test","age":10}' http://localhost:8080/sample/v1/user

Hello test!

$ curl -H 'Content-Type: application/json' -d '{"name":"test_user","age":10}' http://localhost:8080/sample/v1/user

{
  "error": {
    "code": 0,
    "detail": [
      {
        "invalidValue": "test_user",
        "message": "size must be between 3 and 5",
        "messageTemplate": "{javax.validation.constraints.Size.message}",
        "path": "SampleResources.postUser.arg0.name"
      }
    ],
    "message": "constraint violation validate error"
  }
}
```
+ Adding a size constraint for create and update
```
...
type User struct {
    string name (x_size="min=3,max=5,groups=create|update");
    int32 age;
}
...
```
+ Example of using validation groups
```
...

type User struct {
    string name (x_not_null="groups=insert",x_size="min=3,max=5,groups=insert|update");
    string occupation (x_not_null="groups=update", x_size="min=4,groups=update|insert");
    int32 age;
}


resource string POST "/users" {
    User user (x_must_validate="insert");

    ...
}

resource string PUT "/users/{id}" {
    int32 id ;

    User user (x_must_validate="update");

    ....
}

...
```
With the example above, a POST request to /users would be checked against the following rules:

+ The name field is required, the length of its value should be from 3 to 5 chars inclusive.
+ The occupation field is NOT required, but when it is presented the length of its value should be longer than 4 chars
+ The age field is not required and won't be checked

And a PUT request to users/{id} would be checked against the following rules:

+ the name field is NOT required, but when it is presented the length of its value should be from 3 to 5 chars inclusive.
+ the occupation field is required, the length of its value should be longer than 4 chars
+ the age field is not required and wouldn’t be checked

