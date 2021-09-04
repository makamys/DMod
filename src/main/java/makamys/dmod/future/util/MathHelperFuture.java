package makamys.dmod.future.util;

public class MathHelperFuture {
    
    public static float lerp(float delta, float start, float end) {
        return start + delta * (end - start);
    }

    public static double lerp(double delta, double start, double end) {
        return start + delta * (end - start);
    }
    
    @Deprecated
   public static float lerpAngle(float start, float end, float delta) {
      float f;
      for(f = end - start; f < -180.0F; f += 360.0F) {
      }

      while(f >= 180.0F) {
         f -= 360.0F;
      }

      return start + delta * f;
   }
}
