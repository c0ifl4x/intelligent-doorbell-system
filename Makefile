.PHONY: doorbell
doorbell:
	$(MAKE) -C iha-firmware/doorbell run

.PHONY: relay-all
relay-all:
	$(MAKE) -C iha-relay-server/backend run

.PHONY: relay
relay:
	$(MAKE) -C iha-relay-server/backend run-only

.PHONY: relay-ui
relay-ui:
	$(MAKE) -C iha-relay-server/ui dev
