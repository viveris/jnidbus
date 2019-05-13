package Serialization;

import fr.viveris.vizada.jnidbus.serialization.signature.Signature;
import fr.viveris.vizada.jnidbus.serialization.signature.SignatureElement;
import fr.viveris.vizada.jnidbus.serialization.signature.SupportedTypes;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SignatureParserTest {

    @Test
    public void complexSignatureTest(){
        String signature = "issaa(sis)ias(si)";
        Object[] expectations = {
                SupportedTypes.INTEGER,
                SupportedTypes.STRING,
                SupportedTypes.STRING,
                SupportedTypes.ARRAY,
                new Object[]{
                        SupportedTypes.ARRAY,
                        new Object[]{
                                SupportedTypes.OBJECT_BEGIN,
                                new Object[]{
                                        SupportedTypes.STRING,
                                        SupportedTypes.INTEGER,
                                        SupportedTypes.STRING,
                                }
                        }
                },
                SupportedTypes.INTEGER,
                SupportedTypes.ARRAY,
                new Object[]{
                        SupportedTypes.STRING,
                },
                SupportedTypes.OBJECT_BEGIN,
                new Object[]{
                        SupportedTypes.STRING,
                        SupportedTypes.INTEGER,
                },
        };
        Signature object = new Signature(signature);
        int i = 0;
        for(SignatureElement ele : object){
            if(ele.isPrimitive()) assertTrue(ele.getPrimitive() == expectations[i++]);
            else{
                assertTrue(ele.getContainerType() == expectations[i++] );
                assertTrue(checkElement(ele,(Object[]) expectations[i++]));
            }
        }
    }

    private boolean checkElement(SignatureElement element, Object[] values){
        boolean result = true;
        Signature subSignature = element.getSignature();
        int i = 0;
        for(SignatureElement sub : subSignature){
            if(sub.isPrimitive()) assertTrue(sub.getPrimitive() == values[i++]);
            else{
                assertTrue(element.getContainerType() == values[i++] );
                assertTrue(checkElement(sub,(Object[]) values[i++]));
            }
        }
        return result;
    }
}
