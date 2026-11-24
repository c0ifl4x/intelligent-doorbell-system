ROOT_DIR     ?= $(dir $(abspath $(lastword $(MAKEFILE_LIST))))
GRADLEW      := $(ROOT_DIR)gradlew
GRADLE_FLAGS := -q --console=plain

TASK_PREFIX := $(if $(MODULE),$(MODULE):,)

.PHONY: install
install:
	$(GRADLEW) $(TASK_PREFIX)yarnInstall $(GRADLE_FLAGS)

.PHONY: dev
dev:
	$(GRADLEW) $(TASK_PREFIX)yarnDev $(GRADLE_FLAGS)

.PHONY: build
build:
	$(GRADLEW) $(TASK_PREFIX)buildWeb $(GRADLE_FLAGS)

.PHONY: clean
clean:
	$(GRADLEW) $(TASK_PREFIX)clean $(GRADLE_FLAGS)
