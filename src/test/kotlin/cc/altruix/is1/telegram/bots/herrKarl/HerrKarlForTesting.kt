package cc.altruix.is1.telegram.bots.herrKarl

import cc.altruix.is1.jena.IJenaSubsystem
import cc.altruix.is1.telegram.ITelegramCommandRegistry
import cc.altruix.is1.telegram.bots.herrKarl.HerrKarl
import cc.altruix.is1.telegram.bots.knackal.KommissarRex
import org.telegram.telegrambots.api.objects.Message
import org.slf4j.Logger
import cc.altruix.mock

class HerrKarlForTesting : HerrKarl(mock<Logger>(), mock<Logger>(), KommissarRex(mock<IJenaSubsystem>()), mock<ITelegramCommandRegistry>()) {
	override fun handleIncomingMessage(msg: Message) {
		throw Exception("Unexpected call, see test for details")
	}

}