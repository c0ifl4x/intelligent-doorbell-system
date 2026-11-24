#ifndef EHT_W5500_H_
#define EHT_W5500_H_

#include "esp_err.h"
#include "driver/spi_master.h"
#include "driver/gpio.h"

/*! \brief Initial silent wait time for auto-negotiation before alerting the user. */
#define ETH_INIT_WAIT_FOR_LINK_MS       2500
/*! \brief Interval between consecutive link status checks during blocking wait. */
#define ETH_INIT_LINK_CHECK_INTERVAL_MS 1000

/*! \brief Hardware pinout and SPI configuration for the W5500 module. */
typedef struct
{
  spi_host_device_t spi_host; /*!< SPI Host controller instance (e.g. SPI2_HOST). */
  gpio_num_t sclk_gpio;       /*!< GPIO pin for SPI clock (SCK). */
  gpio_num_t mosi_gpio;       /*!< GPIO pin for MOSI. */
  gpio_num_t miso_gpio;       /*!< GPIO pin for MISO. */
  gpio_num_t cs_gpio;         /*!< GPIO pin for Chip Select (CS). */
  gpio_num_t int_gpio;        /*!< GPIO pin for interrupt (INT). */
  gpio_num_t rst_gpio;        /*!< GPIO pin for hardware reset (RST). */
  uint8_t clock_mhz;          /*!< SPI clock speed in MHz. */
} eth_w5500_hw_config_t;

/*! \brief Callbacks for Ethernet events. */
typedef struct
{
  void (*on_link_state_changed)(bool is_up);  /*!< Called when cable is plugged/unplugged. */
  void (*on_packet_received)(void);           /*!< Called on RX activity (useful for blinking LEDs). */
} eth_callbacks_t;

/*! \brief Callback type for boot-time link waiting process.
 *
 * \param state true if link is detected (success), false if still waiting (alert).
 */
typedef void (*eth_link_wait_cb_t)(bool state);

/*! \brief Initializes the W5500 Ethernet module.
 *
 * Sets up SPI bus, MAC, PHY, and attaches the network interface glue. Configures IP from DHCP.
 *
 * \param hw_config Hardware pinout and SPI configuration for the W5500 module.
 * \param callbacks Event handlers for link status and packet activity.
 *
 * \retval ESP_OK         If hardware initialized successfully.
 * \retval ESP_ERR_NO_MEM If no heap memory available.
 * \retval ESP_FAIL       On hardware initialization failure.
 */
esp_err_t eth_w5500_init(const eth_w5500_hw_config_t *hw_config, const eth_callbacks_t *callbacks);

/*! \brief Blocks system execution until a physical Ethernet link is established.
 *
 * First, it waits silently for `ETH_INIT_WAIT_FOR_LINK_MS` to allow auto-negotiation. If no link is found, it enters an
 * alert loop calling `wait_cb` with `false` every `ETH_INIT_LINK_CHECK_INTERVAL_MS` until a cable is connected.
 *
 * \param wait_cb Callback to update UI or indicators during the waiting process.
 */
void eth_w5500_force_link_blocking(eth_link_wait_cb_t wait_cb);

#endif // EHT_W5500_H_
