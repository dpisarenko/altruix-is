package cc.altruix

import org.mockito.Mockito

inline fun<reified T : Any> mock() = Mockito.mock(T::class.java)