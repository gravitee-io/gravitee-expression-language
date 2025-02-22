## [4.0.3](https://github.com/gravitee-io/gravitee-expression-language/compare/4.0.2...4.0.3) (2025-02-22)


### Bug Fixes

* **deps:** define json-smart dependency to force version 2.5.2 ([8250d6b](https://github.com/gravitee-io/gravitee-expression-language/commit/8250d6b24b3fcaf4629faca415680af1677211cb))

## [4.0.2](https://github.com/gravitee-io/gravitee-expression-language/compare/4.0.1...4.0.2) (2025-02-21)


### Bug Fixes

* **deps:** bump gravitee-gateway-api to 3.11.1 ([d28618b](https://github.com/gravitee-io/gravitee-expression-language/commit/d28618b29ea94fe35144645ad9a4ea37ff3b84a3))
* **deps:** bump gravitee-node to 7.0.7 ([1b1bbaf](https://github.com/gravitee-io/gravitee-expression-language/commit/1b1bbaf213439c65eaf492415f76131b82adcd2e))

## [4.0.1](https://github.com/gravitee-io/gravitee-expression-language/compare/4.0.0...4.0.1) (2025-01-23)


### Bug Fixes

* template variable discovery failed to find them all ([5e9e1db](https://github.com/gravitee-io/gravitee-expression-language/commit/5e9e1dbb352f372161412bba5d40a19fc078c3f0))

# [4.0.0](https://github.com/gravitee-io/gravitee-expression-language/compare/3.2.3...4.0.0) (2024-12-30)


### Bug Fixes

* **deps:** bump gravitee-node & gravitee-gateway-api ([fede4b0](https://github.com/gravitee-io/gravitee-expression-language/commit/fede4b0899cf93810535ed6970128264cdd5d2f1))
* updates to support secret ([#116](https://github.com/gravitee-io/gravitee-expression-language/issues/116)) ([0e7c70d](https://github.com/gravitee-io/gravitee-expression-language/commit/0e7c70db7b5f7aed035a8b3a8976737d3e155192))


### Features

* add evalNow to evaluate EL ([9588b34](https://github.com/gravitee-io/gravitee-expression-language/commit/9588b34df20249acd7757d9c37e1aead7ee35fea))
* add while list for secrets.get() methods ([b5526de](https://github.com/gravitee-io/gravitee-expression-language/commit/b5526deb1a42592fcbae8e0c05036198fd38d5c5))
* remove variable provider provide(ctx) method ([dd63fb3](https://github.com/gravitee-io/gravitee-expression-language/commit/dd63fb3b8317878453e8cd2f2f88b6334a1ec704))


### BREAKING CHANGES

* signature change for provide(ExecutionContext)
https://gravitee.atlassian.net/browse/APIM-7417

# [4.0.0-alpha.5](https://github.com/gravitee-io/gravitee-expression-language/compare/4.0.0-alpha.4...4.0.0-alpha.5) (2024-12-30)


### Bug Fixes

* **deps:** bump gravitee-node & gravitee-gateway-api ([fede4b0](https://github.com/gravitee-io/gravitee-expression-language/commit/fede4b0899cf93810535ed6970128264cdd5d2f1))

# [4.0.0-alpha.4](https://github.com/gravitee-io/gravitee-expression-language/compare/4.0.0-alpha.3...4.0.0-alpha.4) (2024-12-30)


### Features

* add while list for secrets.get() methods ([b5526de](https://github.com/gravitee-io/gravitee-expression-language/commit/b5526deb1a42592fcbae8e0c05036198fd38d5c5))

# [4.0.0-alpha.3](https://github.com/gravitee-io/gravitee-expression-language/compare/4.0.0-alpha.2...4.0.0-alpha.3) (2024-12-12)


### Bug Fixes

* updates to support secret ([#116](https://github.com/gravitee-io/gravitee-expression-language/issues/116)) ([0e7c70d](https://github.com/gravitee-io/gravitee-expression-language/commit/0e7c70db7b5f7aed035a8b3a8976737d3e155192))

# [4.0.0-alpha.2](https://github.com/gravitee-io/gravitee-expression-language/compare/4.0.0-alpha.1...4.0.0-alpha.2) (2024-11-21)


### Features

* add evalNow to evaluate EL ([9588b34](https://github.com/gravitee-io/gravitee-expression-language/commit/9588b34df20249acd7757d9c37e1aead7ee35fea))

# [4.0.0-alpha.1](https://github.com/gravitee-io/gravitee-expression-language/compare/3.2.3...4.0.0-alpha.1) (2024-11-21)


### Features

* remove variable provider provide(ctx) method ([dd63fb3](https://github.com/gravitee-io/gravitee-expression-language/commit/dd63fb3b8317878453e8cd2f2f88b6334a1ec704))


### BREAKING CHANGES

* signature change for provide(ExecutionContext)
https://gravitee.atlassian.net/browse/APIM-7417

## [3.2.3](https://github.com/gravitee-io/gravitee-expression-language/compare/3.2.2...3.2.3) (2024-09-02)


### Bug Fixes

* **deps:** bump gravitee-common ([f1dec3d](https://github.com/gravitee-io/gravitee-expression-language/commit/f1dec3d6cfc73cda1167955bfe0767a0d6faf99d))
* **deps:** bump gravitee-gateway-api ([36e41e4](https://github.com/gravitee-io/gravitee-expression-language/commit/36e41e4d744b66608da0cbf316947e65165dcbb1))
* **deps:** bump gravitee-node ([120df98](https://github.com/gravitee-io/gravitee-expression-language/commit/120df98c60598af60e47b9bac1ce9a6ba9ec35cc))

## [3.2.2](https://github.com/gravitee-io/gravitee-expression-language/compare/3.2.1...3.2.2) (2024-09-02)


### Bug Fixes

* **deps:** update dependency com.jayway.jsonpath:json-path to v2.9.0 [security] ([f3b2d1a](https://github.com/gravitee-io/gravitee-expression-language/commit/f3b2d1aa362955d3d34e6a1c069ddafb5c0187c6))

## [3.2.1](https://github.com/gravitee-io/gravitee-expression-language/compare/3.2.0...3.2.1) (2024-08-30)


### Bug Fixes

* document builder should not be namespace aware as namespace are not extracted from EL ([563e152](https://github.com/gravitee-io/gravitee-expression-language/commit/563e152fa1205e84cafb732c3b204436a22daa55))

# [3.2.0](https://github.com/gravitee-io/gravitee-expression-language/compare/3.1.0...3.2.0) (2024-07-04)


### Features

* update whitelist to include base64 encoding and decoding classes ([c141fa2](https://github.com/gravitee-io/gravitee-expression-language/commit/c141fa2bf63b196cbb3ab7eb23e7661bf5ad61b6))

# [3.1.0](https://github.com/gravitee-io/gravitee-expression-language/compare/3.0.2...3.1.0) (2023-07-19)


### Features

* add access to the EL parser for further customization ([9603d6c](https://github.com/gravitee-io/gravitee-expression-language/commit/9603d6cc9a691cfaac5d0c6dfa31fac444e0ba44))

## [3.0.2](https://github.com/gravitee-io/gravitee-expression-language/compare/3.0.1...3.0.2) (2023-07-19)


### Bug Fixes

* **deps:** update dependency com.jayway.jsonpath:json-path to v2.8.0 ([85c0f4c](https://github.com/gravitee-io/gravitee-expression-language/commit/85c0f4cacdf99d9b5ec2ee6abc51993e918a4000))

## [3.0.1](https://github.com/gravitee-io/gravitee-expression-language/compare/3.0.0...3.0.1) (2023-07-19)


### Bug Fixes

* **deps:** update dependency io.gravitee.common:gravitee-common to v2.1.1 ([25316d4](https://github.com/gravitee-io/gravitee-expression-language/commit/25316d4980f9c3d1383ab775dc5e4d2ea6965358))

# [3.0.0](https://github.com/gravitee-io/gravitee-expression-language/compare/2.2.1...3.0.0) (2023-07-18)


### chore

* **deps:** update gravitee-parent ([5cc4b6b](https://github.com/gravitee-io/gravitee-expression-language/commit/5cc4b6b0ba20d1839661f46bca30953712e1011a))


### BREAKING CHANGES

* **deps:** require Java17

## [2.2.1](https://github.com/gravitee-io/gravitee-expression-language/compare/2.2.0...2.2.1) (2023-05-31)


### Bug Fixes

* define classloader in the SpelParser config to avoid exception in the reactive eval method ([b47d978](https://github.com/gravitee-io/gravitee-expression-language/commit/b47d978ec90138327df462bb7be9c9fc441804cf))

# [2.2.0](https://github.com/gravitee-io/gravitee-expression-language/compare/2.1.1...2.2.0) (2023-04-19)


### Features

* bump gravitee-node to use latest cache plugin ([506c745](https://github.com/gravitee-io/gravitee-expression-language/commit/506c74563235acb8e4aca05d5185c5eb54e048da))

## [2.1.1](https://github.com/gravitee-io/gravitee-expression-language/compare/2.1.0...2.1.1) (2023-03-17)


### Bug Fixes

* **deps:** update gateway-api to 2.1.0 ([9d00d46](https://github.com/gravitee-io/gravitee-expression-language/commit/9d00d46cc4b211646b72b0ae661c077c141e912b))

# [2.1.0](https://github.com/gravitee-io/gravitee-expression-language/compare/2.0.1...2.1.0) (2023-03-17)


### Bug Fixes

* **deps:** upgrade gravitee-bom & gravitee-common ([f3ca2b9](https://github.com/gravitee-io/gravitee-expression-language/commit/f3ca2b9cac3385df4f7a6a11fa7ea34c40cc96c2))


### Features

* rename 'jupiter' package in 'reactive' ([29a3977](https://github.com/gravitee-io/gravitee-expression-language/commit/29a39775caac62d033cd73807ca20a12035bdc5f))

# [2.1.0-alpha.1](https://github.com/gravitee-io/gravitee-expression-language/compare/2.0.1...2.1.0-alpha.1) (2023-03-13)


### Features

* rename 'jupiter' package in 'reactive' ([b0812cb](https://github.com/gravitee-io/gravitee-expression-language/commit/b0812cb7f48d22dca7b6955ccf9488f1260482f7))

# [2.1.0-alpha.1](https://github.com/gravitee-io/gravitee-expression-language/compare/2.0.1...2.1.0-alpha.1) (2023-03-09)


### Features

* rename 'jupiter' package in 'reactive' ([b0812cb](https://github.com/gravitee-io/gravitee-expression-language/commit/b0812cb7f48d22dca7b6955ccf9488f1260482f7))

## [2.0.1](https://github.com/gravitee-io/gravitee-expression-language/compare/2.0.0...2.0.1) (2022-12-09)


### Bug Fixes

* use a release version of gateway-api ([976a434](https://github.com/gravitee-io/gravitee-expression-language/commit/976a434e89215b957f8c709456cff5da9e5637c6))

# [2.0.0](https://github.com/gravitee-io/gravitee-expression-language/compare/1.11.4...2.0.0) (2022-12-09)


### Bug Fixes

* bump gateway-api as we don't mix rxJava versions ([3d51d73](https://github.com/gravitee-io/gravitee-expression-language/commit/3d51d7303f1d657fc728946b2f8cdcf2a29dc571))


### chore

* bump to rxJava3 ([139b0b8](https://github.com/gravitee-io/gravitee-expression-language/commit/139b0b82aa3f39a473a98ecb3eba4ba19dab97b5))


### BREAKING CHANGES

* rxJava3 required

# [2.0.0-alpha.2](https://github.com/gravitee-io/gravitee-expression-language/compare/2.0.0-alpha.1...2.0.0-alpha.2) (2022-10-18)


### Bug Fixes

* bump gateway-api as we don't mix rxJava versions ([3d51d73](https://github.com/gravitee-io/gravitee-expression-language/commit/3d51d7303f1d657fc728946b2f8cdcf2a29dc571))

# [2.0.0-alpha.1](https://github.com/gravitee-io/gravitee-expression-language/compare/1.11.3...2.0.0-alpha.1) (2022-10-18)


### chore

* bump to rxJava3 ([139b0b8](https://github.com/gravitee-io/gravitee-expression-language/commit/139b0b82aa3f39a473a98ecb3eba4ba19dab97b5))


### BREAKING CHANGES

* rxJava3 required

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
