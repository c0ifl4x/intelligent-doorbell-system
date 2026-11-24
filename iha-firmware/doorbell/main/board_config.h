#ifndef BOARD_CONFIG_H_
#define BOARD_CONFIG_H_

#include "driver/gpio.h"
#include "driver/spi_master.h"

// W5500 SPI pinout (ESP32-C3)
#define BOARD_ETH_SPI_HOST      SPI2_HOST
#define BOARD_ETH_SCLK_GPIO     GPIO_NUM_4
#define BOARD_ETH_MOSI_GPIO     GPIO_NUM_6
#define BOARD_ETH_MISO_GPIO     GPIO_NUM_5
#define BOARD_ETH_CS_GPIO       GPIO_NUM_7
#define BOARD_ETH_INT_GPIO      GPIO_NUM_1
#define BOARD_ETH_RST_GPIO      GPIO_NUM_3
#define BOARD_ETH_CLOCK_MHZ     20

#endif // BOARD_CONFIG_H_
