ROOT_DIR     ?= $(dir $(abspath $(lastword $(MAKEFILE_LIST))))
GRADLEW      := $(ROOT_DIR)gradlew
GRADLE_FLAGS := -q --console=plain

TASK_PREFIX := $(if $(MODULE),$(MODULE):,)

.PHONY: check-env
check-env:
	$(GRADLEW) $(TASK_PREFIX)checkEnv $(GRADLE_FLAGS)

.PHONY: set-chip
set-chip:
	$(GRADLEW) $(TASK_PREFIX)setChip $(GRADLE_FLAGS)

.PHONY: build
build:
	$(GRADLEW) $(TASK_PREFIX)buildFirmware $(GRADLE_FLAGS)

.PHONY: build-app
build-app:
	$(GRADLEW) $(TASK_PREFIX)buildAppOnly $(GRADLE_FLAGS)

.PHONY: rebuild
rebuild:
	$(GRADLEW) $(TASK_PREFIX)rebuildFirmware $(GRADLE_FLAGS)

.PHONY: flash
flash:
	$(GRADLEW) $(TASK_PREFIX)flashFirmware $(GRADLE_FLAGS)

.PHONY: flash-app
flash-app:
	$(GRADLEW) $(TASK_PREFIX)flashAppOnly $(GRADLE_FLAGS)

.PHONY: monitor
monitor:
	$(GRADLEW) $(TASK_PREFIX)monitorFirmware $(GRADLE_FLAGS)

.PHONY: run
run:
	$(GRADLEW) $(TASK_PREFIX)runFirmware $(GRADLE_FLAGS)

.PHONY: erase
erase:
	$(GRADLEW) $(TASK_PREFIX)eraseFlash $(GRADLE_FLAGS)

.PHONY: clean
clean:
	$(GRADLEW) $(TASK_PREFIX)cleanFirmware $(GRADLE_FLAGS)

.PHONY: docs
docs:
	$(GRADLEW) $(TASK_PREFIX)generateDocs $(GRADLE_FLAGS)
