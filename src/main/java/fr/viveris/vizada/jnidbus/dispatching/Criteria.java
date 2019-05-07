package fr.viveris.vizada.jnidbus.dispatching;

import java.util.Objects;

public class Criteria {

    private String member;
    private String inputSignature;
    private String outputSignature;
    private HandlerType type;

    /**
     *
     * @param member
     * @param inputSignature
     * @param outputSignature used for debug only, as DBus does not do any match on output signature it is just a way for the developer
     *        to quickly see what DBus signature is generated.
     */
    public Criteria(String member, String inputSignature, String outputSignature, HandlerType type) {
        this.member = member;
        this.inputSignature = inputSignature;
        this.outputSignature = outputSignature;
        this.type = type;
    }

    public String getMember() {
        return member;
    }

    public String getInputSignature() {
        return inputSignature;
    }

    public String getOutputSignature() { return outputSignature; }

    public HandlerType getType() { return type; }

    @Override
    public String toString() {
        return "Criteria{" +
                "member='" + member + '\'' +
                ", inputSignature='" + inputSignature + '\'' +
                ", outputSignature='" + outputSignature + '\'' +
                ", type='" + type + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Criteria)) return false;
        Criteria criteria = (Criteria) o;
        return member.equals(criteria.member) &&
                inputSignature.equals(criteria.inputSignature) &&
                type == criteria.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(member, inputSignature, type);
    }

    public enum HandlerType{
        SIGNAL,METHOD
    }
}
