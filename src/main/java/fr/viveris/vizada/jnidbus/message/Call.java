package fr.viveris.vizada.jnidbus.message;

public abstract class Call<In extends Message,Out extends Message> {
    private MethodCall annotation;
    private Class<Out> returnType;
    private In params;

    public Call(In params, Class<Out> returnType){
        this.annotation = this.getClass().getAnnotation(MethodCall.class);
        if(this.annotation== null) throw new IllegalStateException("A DBus method call must be annotated with MethodCall");
        this.returnType = returnType;
        this.params = params;
    }

    public String getDestination(){
        return this.annotation.destination();
    }

    public String getPath(){
        return this.annotation.path();
    }

    public String getInterfaceName(){
        return this.annotation.interfaceName();
    }

    public String getMember(){
        return this.annotation.member();
    }

    public Class<Out> getReturnType() {
        return returnType;
    }

    public In getParams() {
        return params;
    }
}
