
Sample project for demonstrating an issue with Spring Cloud Sleuth and WebClient when using bodyToFlux

### Endpoints

* http://localhost:8080/mono

Uses bodyToMono(), expected result is `{"bar": "bar1"}`.  Works successfully.

* http://localhost:8080/flux

Uses bodyToFlux(), expected result is `[{"bar": "bar1",{"bar": "bar2"}]`.  Actual result is:

`[{"nativeBuffer":{"direct":false,"readOnly":false,"readable":true,"writable":true},"allocated":true}]`

* http://localhost:8080/flux-mapped

Uses bodyToFlux() and a map.  Expected result is `bar1bar2`.  Actual result is a 500 internal error.  Server throws:

`java.lang.ClassCastException: class org.springframework.core.io.buffer.NettyDataBuffer cannot be cast to class com.example.demo.DemoApplication$Foo`


Performing either of the following yields the expected results for all endpoints:
* Reduce Sleuth version from 2.1.3 to 2.1.2
* Remove Spring Cloud Sleuth dependency, re-build, and re-test.



(Not a contribution)
