import fr.viveris.vizada.jnidbus.message.Call;
import fr.viveris.vizada.jnidbus.message.Message;
import fr.viveris.vizada.jnidbus.message.MethodCall;

@MethodCall(
        destination = "fr.viveris.vizada.jnidbus.receiver",
        path = "/test/test",
        interfaceName = "test.test.Interface",
        member = "testMember"
)
public class TestCall extends Call<TestEvent,TestReturn> {

    public TestCall(TestEvent event){
        super(event,TestReturn.class);
    }

}
