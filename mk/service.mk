ROOT_DIR     ?= $(dir $(abspath $(lastword $(MAKEFILE_LIST))))
GRADLEW      := $(ROOT_DIR)gradlew
GRADLE_FLAGS := -q --console=plain

TASK_PREFIX := $(if $(MODULE),$(MODULE):,)

.PHONY: run
run:
	$(GRADLEW) $(TASK_PREFIX)run $(GRADLE_FLAGS)

.PHONY: run-only
run-only:
	$(GRADLEW) $(TASK_PREFIX)run -PskipWebEmbed $(GRADLE_FLAGS)

.PHONY: build
build:
	$(GRADLEW) $(TASK_PREFIX)build $(GRADLE_FLAGS)

.PHONY: clean
clean:
	$(GRADLEW) $(TASK_PREFIX)clean $(GRADLE_FLAGS)

.PHONY: test
test:
	$(GRADLEW) $(TASK_PREFIX)test $(GRADLE_FLAGS)

.PHONY: docker
docker:
	$(GRADLEW) $(TASK_PREFIX)jibDockerBuild $(GRADLE_FLAGS)
