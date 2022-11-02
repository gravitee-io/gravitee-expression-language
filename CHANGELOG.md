## [1.11.4](https://github.com/gravitee-io/gravitee-expression-language/compare/1.11.3...1.11.4) (2022-11-02)


### Bug Fixes

* bump `gravitee-node` to 1.27.3 ([4a70208](https://github.com/gravitee-io/gravitee-expression-language/commit/4a7020804efd9170ede418cfd425fd7fef1efcd4))

## [1.11.3](https://github.com/gravitee-io/gravitee-expression-language/compare/1.11.2...1.11.3) (2022-10-11)


### Bug Fixes

* handle nested EL expressions ([b5a99e5](https://github.com/gravitee-io/gravitee-expression-language/commit/b5a99e5b6a90ce178f8471c39da2b9aa231b9033))

## [1.11.2](https://github.com/gravitee-io/gravitee-expression-language/compare/1.11.1...1.11.2) (2022-09-20)


### Bug Fixes

* allow usage of MultiValueMap.containsAllKeys ([77c64a7](https://github.com/gravitee-io/gravitee-expression-language/commit/77c64a7d27a80176039f896d57aea450761563ab))
* properly handle brackets in evaluated expressions ([80cc2ae](https://github.com/gravitee-io/gravitee-expression-language/commit/80cc2ae9764edaf9c6011f8ff25547011942791a))
* resolve regex with quantifiers ([4881366](https://github.com/gravitee-io/gravitee-expression-language/commit/488136606d33ce0feb0845f9401660443b3b572f)), closes [gravitee-io/issues#8217](https://github.com/gravitee-io/issues/issues/8217)

## [1.11.1](https://github.com/gravitee-io/gravitee-expression-language/compare/1.11.0...1.11.1) (2022-09-20)


### Bug Fixes

* add test for getValue as Boolean and bump gravitee-gateway-api ([ba75187](https://github.com/gravitee-io/gravitee-expression-language/commit/ba751872355abaabf002c9e70bb321a2004711c2))
* restore HTTP headers backward compatibility ([5d8fdb7](https://github.com/gravitee-io/gravitee-expression-language/commit/5d8fdb7c87d5c98322a1aef4a7e83ea7368a8c07))


## [1.9.6](https://github.com/gravitee-io/gravitee-expression-language/compare/1.9.5...1.9.6) (2022-09-14)


### Bug Fixes

* allow usage of MultiValueMap.containsAllKeys ([77c64a7](https://github.com/gravitee-io/gravitee-expression-language/commit/77c64a7d27a80176039f896d57aea450761563ab))
* properly handle brackets in evaluated expressions ([80cc2ae](https://github.com/gravitee-io/gravitee-expression-language/commit/80cc2ae9764edaf9c6011f8ff25547011942791a))

## [1.9.5](https://github.com/gravitee-io/gravitee-expression-language/compare/1.9.4...1.9.5) (2022-08-02)


### Bug Fixes

* resolve regex with quantifiers ([4881366](https://github.com/gravitee-io/gravitee-expression-language/commit/488136606d33ce0feb0845f9401660443b3b572f)), closes [gravitee-io/issues#8217](https://github.com/gravitee-io/issues/issues/8217)

## [1.9.4](https://github.com/gravitee-io/gravitee-expression-language/compare/1.9.3...1.9.4) (2022-07-26)


### Bug Fixes

* add test for getValue as Boolean and bump gravitee-gateway-api ([ba75187](https://github.com/gravitee-io/gravitee-expression-language/commit/ba751872355abaabf002c9e70bb321a2004711c2))

# [1.11.0](https://github.com/gravitee-io/gravitee-expression-language/compare/1.10.1...1.11.0) (2022-08-17)


### Features

* use higher level HttpExecutionContext for TemplateProvider ([8cdc319](https://github.com/gravitee-io/gravitee-expression-language/commit/8cdc31911d2658339ca8cd9cf72c9743870e5485))

## [1.10.1](https://github.com/gravitee-io/gravitee-expression-language/compare/1.10.0...1.10.1) (2022-06-21)


### Bug Fixes

* **spel:** catch parsing exception and return Maybe on error ([5d74a94](https://github.com/gravitee-io/gravitee-expression-language/commit/5d74a94b3fb3f749147b62446f00ea46a6224f6a))

# [1.10.0](https://github.com/gravitee-io/gravitee-expression-language/compare/1.9.2...1.10.0) (2022-06-20)


### Features

* **jupiter:** reactive EL evaluation ([05156e5](https://github.com/gravitee-io/gravitee-expression-language/commit/05156e56e8d367e4e7277605d83912d6b2a13430))
  gravitee-gateway-api ([ba75187](https://github.com/gravitee-io/gravitee-expression-language/commit/ba751872355abaabf002c9e70bb321a2004711c2))

## [1.9.3](https://github.com/gravitee-io/gravitee-expression-language/compare/1.9.2...1.9.3) (2022-06-24)


### Bug Fixes

* restore HTTP headers backward compatibility ([5d8fdb7](https://github.com/gravitee-io/gravitee-expression-language/commit/5d8fdb7c87d5c98322a1aef4a7e83ea7368a8c07))

## [1.9.2](https://github.com/gravitee-io/gravitee-expression-language/compare/1.9.1...1.9.2) (2022-04-05)


### Bug Fixes

* resolve behaviour when try to access to map without values ([f2e0f3c](https://github.com/gravitee-io/gravitee-expression-language/commit/f2e0f3c60aa7bcb052b8f893e1cee8633c59d205)), closes [gravitee-io/issues#7329](https://github.com/gravitee-io/issues/issues/7329)
