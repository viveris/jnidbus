/* Copyright 2019, Viveris Technologies <opensource@toulouse.viveris.fr>
 * Distributed under the terms of the Academic Free License.
 */
package fr.viveris.jnidbus.remote;

import fr.viveris.jnidbus.serialization.Serializable;

/**
 * Class any JNIDBus signal must extend, the generic type will be the data attached to the signal
 * @param <In> data type attached to the signal
 */
public class Signal<In extends Serializable> {
    private In param;

    public Signal(In param) {
        this.param = param;
    }

    public In getParam() {
        return param;
    }
}
