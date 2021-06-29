package makamys.dmod.future;

import java.util.Random;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;

public class PassiveEntityEmulator {
	
	   public static IEntityLivingData postOnSpawnWithEgg(EntityAgeable dis, IEntityLivingData entityData, Random rand) {
		      if (entityData == null) {
		         entityData = new PassiveEntityEmulator.PassiveData(true);
		      }

		      PassiveEntityEmulator.PassiveData passiveData = (PassiveEntityEmulator.PassiveData)entityData;
		      if (passiveData.canSpawnBaby() && passiveData.getSpawnedCount() > 0 && rand.nextFloat() <= passiveData.getBabyChance()) {
		         dis.setGrowingAge(-24000);
		      }

		      passiveData.countSpawned();
		      return entityData;
		      //return super.initialize(world, difficulty, spawnReason, (EntityData)entityData, entityTag);
		   }
	   
	public static class PassiveData implements IEntityLivingData {
		private int spawnCount;
		private final boolean babyAllowed;
		private final float babyChance;

		private PassiveData(boolean bl, float f) {
			this.babyAllowed = bl;
			this.babyChance = f;
		}

		public PassiveData(boolean bl) {
			this(bl, 0.05F);
		}

		public PassiveData(float f) {
			this(true, f);
		}

		public int getSpawnedCount() {
			return this.spawnCount;
		}

		public void countSpawned() {
			++this.spawnCount;
		}

		public boolean canSpawnBaby() {
			return this.babyAllowed;
		}

		public float getBabyChance() {
			return this.babyChance;
		}
	}
	
}
