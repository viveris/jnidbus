package fr.viveris.jnidbus.remote;

import fr.viveris.jnidbus.serialization.Serializable;

public class Signal<In extends Serializable> {
    private In param;

    public Signal(In param) {
        this.param = param;
    }

    public In getParam() {
        return param;
    }
}
