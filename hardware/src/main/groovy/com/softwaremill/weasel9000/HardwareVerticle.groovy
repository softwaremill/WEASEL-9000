package com.softwaremill.weasel9000

import com.pi4j.io.gpio.GpioController
import com.pi4j.io.gpio.GpioFactory
import com.pi4j.io.gpio.GpioPinDigitalInput
import com.pi4j.io.gpio.GpioPinDigitalOutput
import com.pi4j.io.gpio.PinPullResistance
import com.pi4j.io.gpio.PinState
import com.pi4j.io.gpio.RaspiPin
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent
import com.pi4j.io.gpio.event.GpioPinListenerDigital
import groovy.transform.TypeChecked
import org.vertx.java.platform.Verticle

/**
 * @author Jacek Kunicki
 */
@TypeChecked
class HardwareVerticle extends Verticle {
    
    private GpioController gpioController
    private int roomId

    public void start() {
        obtainRoomId()
        createGpioController()
        provisionLikeButton()
        provisionDislikeButton()
    }

    private void obtainRoomId() {
        this.roomId = 1
        println "Room id: ${roomId}"
    }

    private void provisionDislikeButton() {
        final GpioPinDigitalInput dislikeButton = gpioController.provisionDigitalInputPin(RaspiPin.GPIO_13, PinPullResistance.PULL_DOWN)
        final GpioPinDigitalOutput dislikeButtonLight = gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_14, "dislikeButtonLight", PinState.LOW)
        dislikeButton.addListener(new DislikeButtonListener(dislikeButtonLight))
    }

    private void provisionLikeButton() {
        final GpioPinDigitalInput likeButton = gpioController.provisionDigitalInputPin(RaspiPin.GPIO_10, PinPullResistance.PULL_DOWN)
        final GpioPinDigitalOutput likeButtonLight = gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_11, "likeButtonLight", PinState.LOW)
        likeButton.addListener(new LikeButtonListener(likeButtonLight))
    }

    private void createGpioController() {
        this.gpioController = GpioFactory.instance
        println "GPIO loaded"
    }

    abstract class ButtonListener implements GpioPinListenerDigital {

        private static final long BLINK_DELAY = 100 // ms
        private static final long BLINK_DURATION = 500 // ms

        private final GpioPinDigitalOutput light

        protected ButtonListener(GpioPinDigitalOutput light) {
            this.light = light
        }

        protected abstract Weasel.VOTE_TYPE getVoteType()

        @Override
        void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
            if (event.state == PinState.HIGH) {
                def voteType = getVoteType()
                def message = MessageSerializer.serializeMessage(roomId, voteType)
                println "${System.currentTimeMillis()} ${voteType}"
                vertx.eventBus().publish(VertxBuses.BUTTON_BUS, message)
                light.blink(BLINK_DELAY, BLINK_DURATION)
            }
        }
    }

    class LikeButtonListener extends ButtonListener {

        protected LikeButtonListener(GpioPinDigitalOutput light) {
            super(light)
        }

        @Override
        protected Weasel.VOTE_TYPE getVoteType() {
            return Weasel.VOTE_TYPE.LIKE
        }
    }

    class DislikeButtonListener extends ButtonListener {

        protected DislikeButtonListener(GpioPinDigitalOutput light) {
            super(light)
        }

        @Override
        protected Weasel.VOTE_TYPE getVoteType() {
            return Weasel.VOTE_TYPE.DISLIKE
        }
    }
}
