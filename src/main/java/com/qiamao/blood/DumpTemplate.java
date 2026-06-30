import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

public class DumpTemplate {
    public static void main(String[] args) {
        System.out.println("=== Template ===");
        for (Method m : Template.class.getDeclaredMethods()) {
            System.out.println(m);
        }
        System.out.println("=== PlacementSettings ===");
        for (Method m : PlacementSettings.class.getDeclaredMethods()) {
            System.out.println(m);
        }
    }
}
