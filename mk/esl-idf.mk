ROOT_DIR     ?= $(dir $(abspath $(lastword $(MAKEFILE_LIST))))
GRADLEW      := $(ROOT_DIR)gradlew
GRADLE_FLAGS := -q --console=plain

TASK_PREFIX := $(if $(MODULE),$(MODULE):,)

.PHONY: check-env set-chip build build-app rebuild flash flash-app monitor run erase clean docs

check-env:
	$(GRADLEW) $(TASK_PREFIX)checkEnv $(GRADLE_FLAGS)

set-chip:
	$(GRADLEW) $(TASK_PREFIX)setChip $(GRADLE_FLAGS)

build:
	$(GRADLEW) $(TASK_PREFIX)buildFirmware $(GRADLE_FLAGS)

build-app:
	$(GRADLEW) $(TASK_PREFIX)buildAppOnly $(GRADLE_FLAGS)

rebuild:
	$(GRADLEW) $(TASK_PREFIX)rebuildFirmware $(GRADLE_FLAGS)

flash:
	$(GRADLEW) $(TASK_PREFIX)flashFirmware $(GRADLE_FLAGS)

flash-app:
	$(GRADLEW) $(TASK_PREFIX)flashAppOnly $(GRADLE_FLAGS)

monitor:
	$(GRADLEW) $(TASK_PREFIX)monitorFirmware $(GRADLE_FLAGS)

run:
	$(GRADLEW) $(TASK_PREFIX)runFirmware $(GRADLE_FLAGS)

erase:
	$(GRADLEW) $(TASK_PREFIX)eraseFlash $(GRADLE_FLAGS)

clean:
	$(GRADLEW) $(TASK_PREFIX)cleanFirmware $(GRADLE_FLAGS)

docs:
	$(GRADLEW) $(TASK_PREFIX)generateDocs $(GRADLE_FLAGS)
