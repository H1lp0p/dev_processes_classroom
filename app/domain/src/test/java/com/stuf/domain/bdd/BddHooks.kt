package com.stuf.domain.bdd

import com.stuf.domain.bdd.support.BddWorld
import io.cucumber.java.Before

class BddHooks {

    @Before
    fun resetWorld() {
        BddWorld.reset()
    }
}
