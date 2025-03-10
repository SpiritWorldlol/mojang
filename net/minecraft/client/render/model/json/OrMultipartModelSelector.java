package net.minecraft.client.render.model.json;

import com.google.common.collect.Streams;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.state.StateManager;

@Environment(EnvType.CLIENT)
public class OrMultipartModelSelector implements MultipartModelSelector {
   public static final String KEY = "OR";
   private final Iterable selectors;

   public OrMultipartModelSelector(Iterable selectors) {
      this.selectors = selectors;
   }

   public Predicate getPredicate(StateManager arg) {
      List list = (List)Streams.stream(this.selectors).map((selector) -> {
         return selector.getPredicate(arg);
      }).collect(Collectors.toList());
      return (state) -> {
         return list.stream().anyMatch((predicate) -> {
            return predicate.test(state);
         });
      };
   }
}
