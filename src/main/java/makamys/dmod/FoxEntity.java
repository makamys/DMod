package makamys.dmod;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.UUID;

import makamys.dmod.future.AnimalEntityEmulator;
import makamys.dmod.future.AnimalEntityFutured;
import makamys.dmod.future.DiveJumpingGoal;
import makamys.dmod.future.EntityAIModernAvoidEntity;
import makamys.dmod.future.EntityAnimalFuture;
import makamys.dmod.future.EntityLivingFutured;
import makamys.dmod.future.ItemStackFuture;
import makamys.dmod.future.ModernEntityLookHelper;
import makamys.dmod.future.PassiveEntityEmulator;
import net.minecraft.block.BlockColored;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAIRestrictSun;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;

public class FoxEntity extends EntityAnimalFuture {
		private static final int OWNER = 18;
		private static final int OTHER_TRUSTED = 19;
		private static final int TYPE = 20;
		private static final int FOX_FLAGS = 21;
		/*private static final TrackedData<Integer> TYPE;
		private static final TrackedData<Byte> FOX_FLAGS;
		private static final TrackedData<Optional<UUID>> OWNER;
		private static final TrackedData<Optional<UUID>> OTHER_TRUSTED;*/
	   private static final Predicate<EntityItem> PICKABLE_DROP_FILTER;
	   private static final Predicate<Entity> JUST_ATTACKED_SOMETHING_FILTER;
	   private static final Predicate<Entity> CHICKEN_AND_RABBIT_FILTER;
	   private static final Predicate<Entity> NOTICEABLE_PLAYER_FILTER;
	   /*private Goal followChickenAndRabbitGoal;
	   private Goal followBabyTurtleGoal;
	   private Goal followFishGoal;*/
	   private float headRollProgress;
	   private float lastHeadRollProgress;
	   private float extraRollingHeight;
	   private float lastExtraRollingHeight;
	   private int eatingTime;
	   
	   private FoxLookControl lookControl;
	   private FoxMoveControl moveControl;

	   public FoxEntity(World world) {
	      super(world);
	      this.lookControl = new FoxEntity.FoxLookControl();
	      this.moveControl = new FoxEntity.FoxMoveControl();
	      //this.setPathfindingPenalty(PathNodeType.DANGER_OTHER, 0.0F);
	      //this.setPathfindingPenalty(PathNodeType.DAMAGE_OTHER, 0.0F);
	      this.setCanPickUpLoot(true);
	      initGoals();
	   }
	   
	protected void entityInit() {
		super.entityInit();
		this.dataWatcher.addObject(OWNER, String.valueOf(""));
		this.dataWatcher.addObject(OTHER_TRUSTED, String.valueOf(""));
		this.dataWatcher.addObject(TYPE, Byte.valueOf((byte) 0));
		this.dataWatcher.addObject(FOX_FLAGS, Byte.valueOf((byte) 0));
	}

/*
	   protected void initDataTracker() {
	      super.initDataTracker();
	      this.dataTracker.startTracking(OWNER, Optional.empty());
	      this.dataTracker.startTracking(OTHER_TRUSTED, Optional.empty());
	      this.dataTracker.startTracking(TYPE, 0);
	      this.dataTracker.startTracking(FOX_FLAGS, (byte)0);
	   }
*/
	   protected void initGoals() {
	      /*this.followChickenAndRabbitGoal = new FollowTargetGoal(this, AnimalEntity.class, 10, false, false, (livingEntity) -> {
	         return livingEntity instanceof ChickenEntity || livingEntity instanceof RabbitEntity;
	      });
	      this.followBabyTurtleGoal = new FollowTargetGoal(this, TurtleEntity.class, 10, false, false, TurtleEntity.BABY_TURTLE_ON_LAND_FILTER);
	      this.followFishGoal = new FollowTargetGoal(this, FishEntity.class, 20, false, false, (livingEntity) -> {
	         return livingEntity instanceof SchoolingFishEntity;
	      });*/
	      this.tasks.addTask(0, new FoxEntity.FoxSwimGoal());
	      this.tasks.addTask(1, new FoxEntity.StopWanderingGoal());
	      this.tasks.addTask(2, new FoxEntity.EscapeWhenNotAggressiveGoal(2.2D));
	      this.tasks.addTask(3, new FoxEntity.MateGoal(1.0D));
	      this.tasks.addTask(4, new EntityAIModernAvoidEntity(this, EntityPlayer.class, 16.0F, 1.6D, 1.4D, (livingEntity) -> {
	         return NOTICEABLE_PLAYER_FILTER.test(livingEntity) && !this.canTrust(livingEntity.getUniqueID()) && !this.isAggressive();
	      }));
	      this.tasks.addTask(4, new EntityAIModernAvoidEntity(this, EntityWolf.class, 8.0F, 1.6D, 1.4D, (livingEntity) -> {
	         return !((EntityWolf)livingEntity).isTamed() && !this.isAggressive();
	      }));
	      /*this.tasks.addTask(4, new EntityAIModernAvoidEntity(this, PolarBearEntity.class, 8.0F, 1.6D, 1.4D, (livingEntity) -> {
	         return !this.isAggressive();
	      }));*/
	      this.tasks.addTask(5, new FoxEntity.MoveToHuntGoal());
	      this.tasks.addTask(6, new FoxEntity.JumpChasingGoal());
	      this.tasks.addTask(6, new FoxEntity.AvoidDaylightGoal(1.25D));
	      this.tasks.addTask(7, new FoxEntity.AttackGoal(1.2000000476837158D, true));
	      this.tasks.addTask(7, new FoxEntity.DelayedCalmDownGoal());
	      this.tasks.addTask(8, new FoxEntity.FollowParentGoal(this, 1.25D));
	      //this.tasks.addTask(9, new FoxEntity.GoToVillageGoal(32, 200));
	      // TODO
	      //this.tasks.addTask(10, new FoxEntity.EatSweetBerriesGoal(1.2000000476837158D, 12, 2));
	      this.tasks.addTask(10, new EntityAILeapAtTarget(this, 0.4F));
	      this.tasks.addTask(11, new EntityAIWander(this, 1.0D));
	      this.tasks.addTask(11, new FoxEntity.PickupItemGoal());
	      this.tasks.addTask(12, new FoxEntity.LookAtEntityGoal(this, EntityPlayer.class, 24.0F));
	      this.tasks.addTask(13, new FoxEntity.SitDownAndLookAroundGoal());
	      this.targetTasks.addTask(3, new FoxEntity.DefendFriendGoal(EntityLiving.class, false, false, (livingEntity) -> {
	         return JUST_ATTACKED_SOMETHING_FILTER.test(livingEntity) && !this.canTrust(livingEntity.getUniqueID());
	      }));
	   }

	   @Override
	   public String getEatSound(ItemStack stack) {
	      return "SoundEvents.ENTITY_FOX_EAT"; // TODO
	   }
	   
	   public void onLivingUpdate() {
	      if (!this.worldObj.isRemote && this.isEntityAlive()/* && this.canMoveVoluntarily()*/) {
	         ++this.eatingTime;
	         ItemStack itemStack = this.getHeldItem();
	         if (this.canEat(itemStack)) {
	            if (this.eatingTime > 600) {
	               ItemStack itemStack2 = ItemStackFuture.finishUsing(itemStack, this.worldObj, this);
	               if (!ItemStackFuture.isEmpty(itemStack2)) {
	                  this.setCurrentItemOrArmor(0, itemStack2);
	               }

	               this.eatingTime = 0;
	            } else if (this.eatingTime > 560 && this.rand.nextFloat() < 0.1F) {
	               this.playSound(this.getEatSound(itemStack), 1.0F, 1.0F);
	               this.worldObj.setEntityState(this, (byte)45);
	            }
	         }

	         EntityLivingBase livingEntity = this.getAttackTarget();
	         if (livingEntity == null || !livingEntity.isEntityAlive()) {
	            this.setCrouching(false);
	            this.setRollingHead(false);
	         }
	      }

	      if (this.isSleeping() || this.isImmobile()) {
	         this.isJumping = false;
	         this.moveStrafing = 0.0F;
	         this.moveForward = 0.0F;
	      }

	      super.onLivingUpdate();
	      if (this.isAggressive() && this.rand.nextFloat() < 0.05F) {
	         this.playSound("SoundEvents.ENTITY_FOX_AGGRO", 1.0F, 1.0F); //TODO
	      }

	   }
	   
	   // XXX not called
	   protected boolean isImmobile() {
	      return this.isDead;
	   }

	   private boolean canEat(ItemStack stack) {
	      return stack.getItem() instanceof ItemFood && this.getAttackTarget() == null && this.onGround && !this.isSleeping();
	   }

	   protected void initEquipment() {
	      if (this.rand.nextFloat() < 0.2F) {
	         float f = this.rand.nextFloat();
	         ItemStack itemStack6;
	         if (f < 0.05F) {
	            itemStack6 = new ItemStack(Items.emerald);
	         } else if (f < 0.2F) {
	            itemStack6 = new ItemStack(Items.egg);
	         // TODO
	         //} else if (f < 0.4F) {
	         //   itemStack6 = this.rand.nextBoolean() ? new ItemStack(Items.RABBIT_FOOT) : new ItemStack(Items.RABBIT_HIDE);
	         } else if (f < 0.6F) {
	            itemStack6 = new ItemStack(Items.wheat);
	         } else if (f < 0.8F) {
	            itemStack6 = new ItemStack(Items.leather);
	         } else {
	            itemStack6 = new ItemStack(Items.feather);
	         }

	         this.setCurrentItemOrArmor(0, itemStack6);
	      }

	   }

	   @SideOnly(Side.CLIENT)
	public void handleHealthUpdate(byte status) {
		if (status == 45) {
			ItemStack itemStack = this.getHeldItem();
			if (!ItemStackFuture.isEmpty(itemStack)) {
				for (int i = 0; i < 8; ++i) {
					Vec3 vec3d = (Vec3.createVectorHelper(((double) this.rand.nextFloat() - 0.5D) * 0.1D,
							Math.random() * 0.1D + 0.1D, 0.0D));
					vec3d.rotateAroundX(-this.rotationPitch * 0.017453292F);
					vec3d.rotateAroundY(-this.rotationYaw * 0.017453292F);
					// TODO
					/*this.worldObj.spawnParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, itemStack),
							this.getX() + this.getRotationVector().x / 2.0D, this.getY(),
							this.getZ() + this.getRotationVector().z / 2.0D, vec3d.x, vec3d.y + 0.05D, vec3d.z);*/
				}
			}
		} else {
			super.handleHealthUpdate(status);
		}

	}

		protected void applyEntityAttributes() {
			super.applyEntityAttributes();
			this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(10.0D);
			this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.30000001192092896D);
			this.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(32D);
			this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(2D);
		}
	   
	   public FoxEntity createChild(EntityAgeable passiveEntity) {
	      FoxEntity foxEntity = new FoxEntity(this.worldObj);
	      foxEntity.setType(this.rand.nextBoolean() ? this.getFoxType() : ((FoxEntity)passiveEntity).getFoxType());
	      return foxEntity;
	   }

	   public IEntityLivingData onSpawnWithEgg(IEntityLivingData entityData){
		   entityData = super.onSpawnWithEgg(entityData);
	      Optional<BiomeGenBase> optional = Optional.of(worldObj.getBiomeGenForCoords((int)this.posX, (int)this.posZ));
	      FoxEntity.Type type = FoxEntity.Type.fromBiome(optional);
	      boolean bl = false;
	      if (entityData instanceof FoxEntity.FoxData) {
	         type = ((FoxEntity.FoxData)entityData).type;
	         if (((FoxEntity.FoxData)entityData).getSpawnedCount() >= 2) {
	            bl = true;
	         }
	      } else {
	         entityData = new FoxEntity.FoxData(type);
	      }

	      this.setType(type);
	      if (bl) {
	         this.setGrowingAge(-24000);
	      }

	      if (worldObj instanceof WorldServer) {
	         this.addTypeSpecificGoals();
	      }

	      this.initEquipment(/*difficulty*/);
	      return PassiveEntityEmulator.postOnSpawnWithEgg(this, entityData, rand);
	   }

	   private void addTypeSpecificGoals() {
		   // TODO
		   /*
	      if (this.getFoxType() == FoxEntity.Type.RED) {
	         this.targetTasks.addTask(4, this.followChickenAndRabbitGoal);
	         this.targetTasks.addTask(4, this.followBabyTurtleGoal);
	         this.targetTasks.addTask(6, this.followFishGoal);
	      } else {
	         this.targetTasks.addTask(4, this.followFishGoal);
	         this.targetTasks.addTask(6, this.followChickenAndRabbitGoal);
	         this.targetTasks.addTask(6, this.followBabyTurtleGoal);
	      }
*/
	   }

	   public void eat(EntityPlayer player, ItemStack stack) {
	      if (this.isBreedingItem(stack)) {
	         this.playSound(this.getEatSound(stack), 1.0F, 1.0F);
	      }

	      super.eat(player, stack);
	   }

	   public float getEyeHeight() {
	      return this.isChild() ? this.height * 0.85F : 0.4F;
	   }

	   public FoxEntity.Type getFoxType() {
	      return FoxEntity.Type.fromId(dataWatcher.getWatchableObjectByte(TYPE));
	   }

	   private void setType(FoxEntity.Type type) {
	      this.dataWatcher.updateObject(TYPE, Byte.valueOf((byte)type.getId()));
	   }

	   private List<UUID> getTrustedUuids() {
	      List<UUID> list = Lists.newArrayList();
	      list.add(DUtil.UUIDorNullFromString(this.dataWatcher.getWatchableObjectString(OWNER)));
	      list.add(DUtil.UUIDorNullFromString(this.dataWatcher.getWatchableObjectString(OTHER_TRUSTED)));
	      return list;
	   }

	   private void addTrustedUuid(@Nullable UUID uuid) {
	      if (!this.dataWatcher.getWatchableObjectString(OWNER).isEmpty()) {
	         this.dataWatcher.updateObject(OTHER_TRUSTED, uuid.toString());
	      } else {
	         this.dataWatcher.updateObject(OWNER, uuid.toString());
	      }
	   }
	   
	/**
	 * (abstract) Protected helper method to write subclass entity data to NBT.
	 */
	public void writeEntityToNBT(NBTTagCompound tag) {
		super.writeEntityToNBT(tag);
		List<UUID> list = this.getTrustedUuids();
		NBTTagList listTag = new NBTTagList();
		Iterator<UUID> var4 = list.iterator();

		while (var4.hasNext()) {
			UUID uUID = var4.next();
			if (uUID != null) {
				listTag.appendTag(new NBTTagString(uUID.toString()));
			}
		}

		tag.setTag("Trusted", listTag);
		tag.setBoolean("Sleeping", this.isSleeping());
		tag.setString("Type", this.getFoxType().getKey());
		tag.setBoolean("Sitting", this.isSitting());
		tag.setBoolean("Crouching", this.isInSneakingPose());
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	public void readEntityFromNBT(NBTTagCompound tag) {
		super.readEntityFromNBT(tag);
		NBTTagList listTag = tag.getTagList("Trusted", DConstants.NBT_TYPE_STRING);
		
		for(int i = 0; i < listTag.tagCount(); ++i) {
	         this.addTrustedUuid(UUID.fromString(listTag.getStringTagAt(i)));
	      }

	      this.setSleeping(tag.getBoolean("Sleeping"));
	      this.setType(FoxEntity.Type.byName(tag.getString("Type")));
	      this.setSitting(tag.getBoolean("Sitting"));
	      this.setCrouching(tag.getBoolean("Crouching"));
	      if (this.worldObj instanceof WorldServer) {
	         this.addTypeSpecificGoals();
	      }
	}

	   public boolean isSitting() {
	      return this.getFoxFlag(1);
	   }

	   public void setSitting(boolean sitting) {
	      this.setFoxFlag(1, sitting);
	   }

	   public boolean isWalking() {
	      return this.getFoxFlag(64);
	   }

	   private void setWalking(boolean walking) {
	      this.setFoxFlag(64, walking);
	   }

	   private boolean isAggressive() {
	      return this.getFoxFlag(128);
	   }

	   private void setAggressive(boolean aggressive) {
	      this.setFoxFlag(128, aggressive);
	   }

	   public boolean isSleeping() {
	      return this.getFoxFlag(32);
	   }

	   private void setSleeping(boolean sleeping) {
	      this.setFoxFlag(32, sleeping);
	   }

	   private void setFoxFlag(int mask, boolean value) {
	      if (value) {
	         this.dataWatcher.updateObject(FOX_FLAGS, (byte)((Byte)this.dataWatcher.getWatchableObjectByte(FOX_FLAGS) | mask));
	      } else {
	         this.dataWatcher.updateObject(FOX_FLAGS, (byte)((Byte)this.dataWatcher.getWatchableObjectByte(FOX_FLAGS) & ~mask));
	      }

	   }

	   private boolean getFoxFlag(int bitmask) {
	      return ((Byte)this.dataWatcher.getWatchableObjectByte(FOX_FLAGS) & bitmask) != 0;
	   }
	   
	   // TODO for dispenser support
	   /*
	   public boolean canEquip(ItemStack stack) {
	      EquipmentSlot equipmentSlot = MobEntity.getPreferredEquipmentSlot(stack);
	      if (!this.getEquippedStack(equipmentSlot).isEmpty()) {
	         return false;
	      } else {
	         return equipmentSlot == EquipmentSlot.MAINHAND && super.canEquip(stack);
	      }
	   }
*/
	   public boolean canPickupItem(ItemStack stack) {
	      Item item = stack.getItem();
	      ItemStack itemStack = this.getEquippedStack(EquipmentSlot.MAINHAND);
	      return itemStack.isEmpty() || this.eatingTime > 0 && item.isFood() && !itemStack.getItem().isFood();
	   }

	   private void spit(ItemStack stack) {
	      if (!stack.isEmpty() && !this.world.isClient) {
	         ItemEntity itemEntity = new ItemEntity(this.world, this.getX() + this.getRotationVector().x, this.getY() + 1.0D, this.getZ() + this.getRotationVector().z, stack);
	         itemEntity.setPickupDelay(40);
	         itemEntity.setThrower(this.getUuid());
	         this.playSound(SoundEvents.ENTITY_FOX_SPIT, 1.0F, 1.0F);
	         this.world.spawnEntity(itemEntity);
	      }
	   }

	   private void dropItem(ItemStack stack) {
	      ItemEntity itemEntity = new ItemEntity(this.world, this.getX(), this.getY(), this.getZ(), stack);
	      this.world.spawnEntity(itemEntity);
	   }

	   protected void loot(ItemEntity item) {
	      ItemStack itemStack = item.getStack();
	      if (this.canPickupItem(itemStack)) {
	         int i = itemStack.getCount();
	         if (i > 1) {
	            this.dropItem(itemStack.split(i - 1));
	         }

	         this.spit(this.getEquippedStack(EquipmentSlot.MAINHAND));
	         this.method_29499(item);
	         this.equipStack(EquipmentSlot.MAINHAND, itemStack.split(1));
	         this.handDropChances[EquipmentSlot.MAINHAND.getEntitySlotId()] = 2.0F;
	         this.sendPickup(item, itemStack.getCount());
	         item.remove();
	         this.eatingTime = 0;
	      }

	   }

	   public void tick() {
	      super.tick();
	      if (this.canMoveVoluntarily()) {
	         boolean bl = this.isTouchingWater();
	         if (bl || this.getTarget() != null || this.world.isThundering()) {
	            this.stopSleeping();
	         }

	         if (bl || this.isSleeping()) {
	            this.setSitting(false);
	         }

	         if (this.isWalking() && this.world.random.nextFloat() < 0.2F) {
	            BlockPos blockPos = this.getBlockPos();
	            BlockState blockState = this.world.getBlockState(blockPos);
	            this.world.syncWorldEvent(2001, blockPos, Block.getRawIdFromState(blockState));
	         }
	      }

	      this.lastHeadRollProgress = this.headRollProgress;
	      if (this.isRollingHead()) {
	         this.headRollProgress += (1.0F - this.headRollProgress) * 0.4F;
	      } else {
	         this.headRollProgress += (0.0F - this.headRollProgress) * 0.4F;
	      }

	      this.lastExtraRollingHeight = this.extraRollingHeight;
	      if (this.isInSneakingPose()) {
	         this.extraRollingHeight += 0.2F;
	         if (this.extraRollingHeight > 3.0F) {
	            this.extraRollingHeight = 3.0F;
	         }
	      } else {
	         this.extraRollingHeight = 0.0F;
	      }

	   }

	   public boolean isBreedingItem(ItemStack stack) {
	      return stack.getItem() == Items.SWEET_BERRIES;
	   }

	   protected void onPlayerSpawnedChild(PlayerEntity player, MobEntity child) {
	      ((FoxEntity)child).addTrustedUuid(player.getUuid());
	   }

	   public boolean isChasing() {
	      return this.getFoxFlag(16);
	   }

	   public void setChasing(boolean chasing) {
	      this.setFoxFlag(16, chasing);
	   }

	   public boolean isFullyCrouched() {
	      return this.extraRollingHeight == 3.0F;
	   }

	   public void setCrouching(boolean crouching) {
	      this.setFoxFlag(4, crouching);
	   }

	   public boolean isInSneakingPose() {
	      return this.getFoxFlag(4);
	   }

	   public void setRollingHead(boolean rollingHead) {
	      this.setFoxFlag(8, rollingHead);
	   }

	   public boolean isRollingHead() {
	      return this.getFoxFlag(8);
	   }

	   @Environment(EnvType.CLIENT)
	   public float getHeadRoll(float tickDelta) {
	      return MathHelper.lerp(tickDelta, this.lastHeadRollProgress, this.headRollProgress) * 0.11F * 3.1415927F;
	   }

	   @Environment(EnvType.CLIENT)
	   public float getBodyRotationHeightOffset(float tickDelta) {
	      return MathHelper.lerp(tickDelta, this.lastExtraRollingHeight, this.extraRollingHeight);
	   }

	   public void setTarget(@Nullable LivingEntity target) {
	      if (this.isAggressive() && target == null) {
	         this.setAggressive(false);
	      }

	      super.setTarget(target);
	   }

	   protected int computeFallDamage(float fallDistance, float damageMultiplier) {
	      return MathHelper.ceil((fallDistance - 5.0F) * damageMultiplier);
	   }

	   private void stopSleeping() {
	      this.setSleeping(false);
	   }

	   private void stopActions() {
	      this.setRollingHead(false);
	      this.setCrouching(false);
	      this.setSitting(false);
	      this.setSleeping(false);
	      this.setAggressive(false);
	      this.setWalking(false);
	   }

	   private boolean wantsToPickupItem() {
	      return !this.isSleeping() && !this.isSitting() && !this.isWalking();
	   }

	   public void playAmbientSound() {
	      SoundEvent soundEvent = this.getAmbientSound();
	      if (soundEvent == SoundEvents.ENTITY_FOX_SCREECH) {
	         this.playSound(soundEvent, 2.0F, this.getSoundPitch());
	      } else {
	         super.playAmbientSound();
	      }

	   }

	   @Nullable
	   protected SoundEvent getAmbientSound() {
	      if (this.isSleeping()) {
	         return SoundEvents.ENTITY_FOX_SLEEP;
	      } else {
	         if (!this.world.isDay() && this.random.nextFloat() < 0.1F) {
	            List list = this.world.getEntitiesByClass(PlayerEntity.class, this.getBoundingBox().expand(16.0D, 16.0D, 16.0D), EntityPredicates.EXCEPT_SPECTATOR);
	            if (list.isEmpty()) {
	               return SoundEvents.ENTITY_FOX_SCREECH;
	            }
	         }

	         return SoundEvents.ENTITY_FOX_AMBIENT;
	      }
	   }

	   @Nullable
	   protected SoundEvent getHurtSound(DamageSource source) {
	      return SoundEvents.ENTITY_FOX_HURT;
	   }

	   @Nullable
	   protected SoundEvent getDeathSound() {
	      return SoundEvents.ENTITY_FOX_DEATH;
	   }

	   private boolean canTrust(UUID uuid) {
	      return this.getTrustedUuids().contains(uuid);
	   }

	   protected void drop(DamageSource source) {
	      ItemStack itemStack = this.getEquippedStack(EquipmentSlot.MAINHAND);
	      if (!itemStack.isEmpty()) {
	         this.dropStack(itemStack);
	         this.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
	      }

	      super.drop(source);
	   }

	   public static boolean canJumpChase(FoxEntity fox, LivingEntity chasedEntity) {
	      double d = chasedEntity.getZ() - fox.getZ();
	      double e = chasedEntity.getX() - fox.getX();
	      double f = d / e;
	      int i = true;

	      for(int j = 0; j < 6; ++j) {
	         double g = f == 0.0D ? 0.0D : d * (double)((float)j / 6.0F);
	         double h = f == 0.0D ? e * (double)((float)j / 6.0F) : g / f;

	         for(int k = 1; k < 4; ++k) {
	            if (!fox.world.getBlockState(new BlockPos(fox.getX() + h, fox.getY() + (double)k, fox.getZ() + g)).getMaterial().isReplaceable()) {
	               return false;
	            }
	         }
	      }

	      return true;
	   }

	   @Environment(EnvType.CLIENT)
	   public Vec3d method_29919() {
	      return new Vec3d(0.0D, (double)(0.55F * this.getStandingEyeHeight()), (double)(this.getWidth() * 0.4F));
	   }

	   // $FF: synthetic method
	   public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
	      return this.createChild(world, entity);
	   }

	   static {
	      TYPE = DataTracker.registerData(FoxEntity.class, TrackedDataHandlerRegistry.INTEGER);
	      FOX_FLAGS = DataTracker.registerData(FoxEntity.class, TrackedDataHandlerRegistry.BYTE);
	      OWNER = DataTracker.registerData(FoxEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
	      OTHER_TRUSTED = DataTracker.registerData(FoxEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
	      PICKABLE_DROP_FILTER = (itemEntity) -> {
	         return !itemEntity.cannotPickup() && itemEntity.isAlive();
	      };
	      JUST_ATTACKED_SOMETHING_FILTER = (entity) -> {
	         if (!(entity instanceof LivingEntity)) {
	            return false;
	         } else {
	            LivingEntity livingEntity = (LivingEntity)entity;
	            return livingEntity.getAttacking() != null && livingEntity.getLastAttackTime() < livingEntity.age + 600;
	         }
	      };
	      CHICKEN_AND_RABBIT_FILTER = (entity) -> {
	         return entity instanceof ChickenEntity || entity instanceof RabbitEntity;
	      };
	      NOTICEABLE_PLAYER_FILTER = (entity) -> {
	         return !entity.isSneaky() && EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.test(entity);
	      };
	   }

	   class LookAtEntityGoal extends EntityAIWatchClosest {
	      public LookAtEntityGoal(EntityLiving fox, Class targetType, float range) {
	         super(fox, targetType, range);
	      }

	      public boolean canStart() {
	         return super.canStart() && !FoxEntity.this.isWalking() && !FoxEntity.this.isRollingHead();
	      }

	      public boolean shouldContinue() {
	         return super.shouldContinue() && !FoxEntity.this.isWalking() && !FoxEntity.this.isRollingHead();
	      }
	   }

	   class FollowParentGoal extends EntityAIFollowParent {
	      private final FoxEntity fox;

	      public FollowParentGoal(FoxEntity fox, double speed) {
	         super(fox, speed);
	         this.fox = fox;
	      }

	      public boolean canStart() {
	         return !this.fox.isAggressive() && super.canStart();
	      }

	      public boolean shouldContinue() {
	         return !this.fox.isAggressive() && super.shouldContinue();
	      }

	      public void start() {
	         this.fox.stopActions();
	         super.start();
	      }
	   }

	   public class FoxLookControl extends LookControl {
	      public FoxLookControl() {
	         super(FoxEntity.this);
	      }

	      public void tick() {
	         if (!FoxEntity.this.isSleeping()) {
	            super.tick();
	         }

	      }

	      protected boolean shouldStayHorizontal() {
	         return !FoxEntity.this.isChasing() && !FoxEntity.this.isInSneakingPose() && !FoxEntity.this.isRollingHead() & !FoxEntity.this.isWalking();
	      }
	   }

	   public class JumpChasingGoal extends DiveJumpingGoal {
	      public boolean canStart() {
	         if (!FoxEntity.this.isFullyCrouched()) {
	            return false;
	         } else {
	            LivingEntity livingEntity = FoxEntity.this.getTarget();
	            if (livingEntity != null && livingEntity.isAlive()) {
	               if (livingEntity.getMovementDirection() != livingEntity.getHorizontalFacing()) {
	                  return false;
	               } else {
	                  boolean bl = FoxEntity.canJumpChase(FoxEntity.this, livingEntity);
	                  if (!bl) {
	                     FoxEntity.this.getNavigation().findPathTo((Entity)livingEntity, 0);
	                     FoxEntity.this.setCrouching(false);
	                     FoxEntity.this.setRollingHead(false);
	                  }

	                  return bl;
	               }
	            } else {
	               return false;
	            }
	         }
	      }

	      public boolean shouldContinue() {
	         LivingEntity livingEntity = FoxEntity.this.getTarget();
	         if (livingEntity != null && livingEntity.isAlive()) {
	            double d = FoxEntity.this.getVelocity().y;
	            return (d * d >= 0.05000000074505806D || Math.abs(FoxEntity.this.pitch) >= 15.0F || !FoxEntity.this.onGround) && !FoxEntity.this.isWalking();
	         } else {
	            return false;
	         }
	      }

	      public boolean canStop() {
	         return false;
	      }

	      public void start() {
	         FoxEntity.this.setJumping(true);
	         FoxEntity.this.setChasing(true);
	         FoxEntity.this.setRollingHead(false);
	         LivingEntity livingEntity = FoxEntity.this.getTarget();
	         FoxEntity.this.getLookControl().lookAt(livingEntity, 60.0F, 30.0F);
	         Vec3d vec3d = (new Vec3d(livingEntity.getX() - FoxEntity.this.getX(), livingEntity.getY() - FoxEntity.this.getY(), livingEntity.getZ() - FoxEntity.this.getZ())).normalize();
	         FoxEntity.this.setVelocity(FoxEntity.this.getVelocity().add(vec3d.x * 0.8D, 0.9D, vec3d.z * 0.8D));
	         FoxEntity.this.getNavigation().stop();
	      }

	      public void stop() {
	         FoxEntity.this.setCrouching(false);
	         FoxEntity.this.extraRollingHeight = 0.0F;
	         FoxEntity.this.lastExtraRollingHeight = 0.0F;
	         FoxEntity.this.setRollingHead(false);
	         FoxEntity.this.setChasing(false);
	      }

	      public void tick() {
	         LivingEntity livingEntity = FoxEntity.this.getTarget();
	         if (livingEntity != null) {
	            FoxEntity.this.getLookControl().lookAt(livingEntity, 60.0F, 30.0F);
	         }

	         if (!FoxEntity.this.isWalking()) {
	            Vec3d vec3d = FoxEntity.this.getVelocity();
	            if (vec3d.y * vec3d.y < 0.029999999329447746D && FoxEntity.this.pitch != 0.0F) {
	               FoxEntity.this.pitch = MathHelper.lerpAngle(FoxEntity.this.pitch, 0.0F, 0.2F);
	            } else {
	               double d = Math.sqrt(Entity.squaredHorizontalLength(vec3d));
	               double e = Math.signum(-vec3d.y) * Math.acos(d / vec3d.length()) * 57.2957763671875D;
	               FoxEntity.this.pitch = (float)e;
	            }
	         }

	         if (livingEntity != null && FoxEntity.this.distanceTo(livingEntity) <= 2.0F) {
	            FoxEntity.this.tryAttack(livingEntity);
	         } else if (FoxEntity.this.pitch > 0.0F && FoxEntity.this.onGround && (float)FoxEntity.this.getVelocity().y != 0.0F && FoxEntity.this.world.getBlockState(FoxEntity.this.getBlockPos()).isOf(Blocks.SNOW)) {
	            FoxEntity.this.pitch = 60.0F;
	            FoxEntity.this.setTarget((LivingEntity)null);
	            FoxEntity.this.setWalking(true);
	         }

	      }
	   }

	   class FoxSwimGoal extends EntityAISwimming {
	      public FoxSwimGoal() {
	         super(FoxEntity.this);
	      }
	      
	      @Override
	      public void startExecuting() {
	         super.startExecuting();
	         FoxEntity.this.stopActions();
	      }
	      
	      @Override
	      public boolean shouldExecute() {
	         return FoxEntity.this.isTouchingWater() && FoxEntity.this.getFluidHeight(FluidTags.WATER) > 0.25D || FoxEntity.this.isInLava();
	      }
	   }

	   /*class GoToVillageGoal extends net.minecraft.entity.ai.goal.GoToVillageGoal {
	      public GoToVillageGoal(int unused, int searchRange) {
	         super(FoxEntity.this, searchRange);
	      }

	      public void start() {
	         FoxEntity.this.stopActions();
	         super.start();
	      }

	      public boolean canStart() {
	         return super.canStart() && this.canGoToVillage();
	      }

	      public boolean shouldContinue() {
	         return super.shouldContinue() && this.canGoToVillage();
	      }

	      private boolean canGoToVillage() {
	         return !FoxEntity.this.isSleeping() && !FoxEntity.this.isSitting() && !FoxEntity.this.isAggressive() && FoxEntity.this.getTarget() == null;
	      }
	   }*/

	   class EscapeWhenNotAggressiveGoal extends EntityAIPanic {
	      public EscapeWhenNotAggressiveGoal(double speed) {
	         super(FoxEntity.this, speed);
	      }
	      
	      @Override
	      public boolean canStart() {
	         return !FoxEntity.this.isAggressive() && super.canStart();
	      }
	   }

	   class StopWanderingGoal extends EntityAIBase {
	      int timer;

	      public StopWanderingGoal() {
	         this.setControls(EnumSet.of(Goal.Control.LOOK, Goal.Control.JUMP, Goal.Control.MOVE));
	      }

	      public boolean canStart() {
	         return FoxEntity.this.isWalking();
	      }

	      public boolean shouldContinue() {
	         return this.canStart() && this.timer > 0;
	      }

	      public void start() {
	         this.timer = 40;
	      }

	      public void stop() {
	         FoxEntity.this.setWalking(false);
	      }

	      public void tick() {
	         --this.timer;
	      }
	   }
	   
	   // implement IEntityAdditionalSpawnData?
	   public static class FoxData extends PassiveEntityEmulator.PassiveData {
	      public final FoxEntity.Type type;

	      public FoxData(FoxEntity.Type type) {
	    	  super(false);
	         this.type = type;
	      }
	   }
/*
	   public class EatSweetBerriesGoal extends MoveToTargetPosGoal {
	      protected int timer;

	      public EatSweetBerriesGoal(double speed, int range, int maxYDifference) {
	         super(FoxEntity.this, speed, range, maxYDifference);
	      }

	      public double getDesiredSquaredDistanceToTarget() {
	         return 2.0D;
	      }

	      public boolean shouldResetPath() {
	         return this.tryingTime % 100 == 0;
	      }

	      protected boolean isTargetPos(WorldView world, BlockPos pos) {
	         BlockState blockState = world.getBlockState(pos);
	         return blockState.isOf(Blocks.SWEET_BERRY_BUSH) && (Integer)blockState.get(SweetBerryBushBlock.AGE) >= 2;
	      }

	      public void tick() {
	         if (this.hasReached()) {
	            if (this.timer >= 40) {
	               this.eatSweetBerry();
	            } else {
	               ++this.timer;
	            }
	         } else if (!this.hasReached() && FoxEntity.this.random.nextFloat() < 0.05F) {
	            FoxEntity.this.playSound(SoundEvents.ENTITY_FOX_SNIFF, 1.0F, 1.0F);
	         }

	         super.tick();
	      }

	      protected void eatSweetBerry() {
	         if (FoxEntity.this.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
	            BlockState blockState = FoxEntity.this.world.getBlockState(this.targetPos);
	            if (blockState.isOf(Blocks.SWEET_BERRY_BUSH)) {
	               int i = (Integer)blockState.get(SweetBerryBushBlock.AGE);
	               blockState.with(SweetBerryBushBlock.AGE, 1);
	               int j = 1 + FoxEntity.this.world.random.nextInt(2) + (i == 3 ? 1 : 0);
	               ItemStack itemStack = FoxEntity.this.getEquippedStack(EquipmentSlot.MAINHAND);
	               if (itemStack.isEmpty()) {
	                  FoxEntity.this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.SWEET_BERRIES));
	                  --j;
	               }

	               if (j > 0) {
	                  Block.dropStack(FoxEntity.this.world, this.targetPos, new ItemStack(Items.SWEET_BERRIES, j));
	               }

	               FoxEntity.this.playSound(SoundEvents.ITEM_SWEET_BERRIES_PICK_FROM_BUSH, 1.0F, 1.0F);
	               FoxEntity.this.world.setBlockState(this.targetPos, (BlockState)blockState.with(SweetBerryBushBlock.AGE, 1), 2);
	            }
	         }
	      }

	      public boolean canStart() {
	         return !FoxEntity.this.isSleeping() && super.canStart();
	      }

	      public void start() {
	         this.timer = 0;
	         FoxEntity.this.setSitting(false);
	         super.start();
	      }
	   }
*/
	   class SitDownAndLookAroundGoal extends FoxEntity.CalmDownGoal {
	      private double lookX;
	      private double lookZ;
	      private int timer;
	      private int counter;

	      public SitDownAndLookAroundGoal() {
	         super(null);
	         this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
	      }

	      public boolean canStart() {
	         return FoxEntity.this.getAttacker() == null && FoxEntity.this.getRandom().nextFloat() < 0.02F && !FoxEntity.this.isSleeping() && FoxEntity.this.getTarget() == null && FoxEntity.this.getNavigation().isIdle() && !this.canCalmDown() && !FoxEntity.this.isChasing() && !FoxEntity.this.isInSneakingPose();
	      }

	      public boolean shouldContinue() {
	         return this.counter > 0;
	      }

	      public void start() {
	         this.chooseNewAngle();
	         this.counter = 2 + FoxEntity.this.getRandom().nextInt(3);
	         FoxEntity.this.setSitting(true);
	         FoxEntity.this.getNavigation().stop();
	      }

	      public void stop() {
	         FoxEntity.this.setSitting(false);
	      }

	      public void tick() {
	         --this.timer;
	         if (this.timer <= 0) {
	            --this.counter;
	            this.chooseNewAngle();
	         }

	         FoxEntity.this.getLookControl().lookAt(FoxEntity.this.getX() + this.lookX, FoxEntity.this.getEyeY(), FoxEntity.this.getZ() + this.lookZ, (float)FoxEntity.this.getBodyYawSpeed(), (float)FoxEntity.this.getLookPitchSpeed());
	      }

	      private void chooseNewAngle() {
	         double d = 6.283185307179586D * FoxEntity.this.getRandom().nextDouble();
	         this.lookX = Math.cos(d);
	         this.lookZ = Math.sin(d);
	         this.timer = 80 + FoxEntity.this.getRandom().nextInt(20);
	      }
	   }

	   class DelayedCalmDownGoal extends FoxEntity.CalmDownGoal {
	      private int timer;

	      public DelayedCalmDownGoal() {
	         super(null);
	         this.timer = FoxEntity.this.random.nextInt(140);
	         this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK, Goal.Control.JUMP));
	      }

	      public boolean canStart() {
	         if (FoxEntity.this.sidewaysSpeed == 0.0F && FoxEntity.this.upwardSpeed == 0.0F && FoxEntity.this.forwardSpeed == 0.0F) {
	            return this.canNotCalmDown() || FoxEntity.this.isSleeping();
	         } else {
	            return false;
	         }
	      }

	      public boolean shouldContinue() {
	         return this.canNotCalmDown();
	      }

	      private boolean canNotCalmDown() {
	         if (this.timer > 0) {
	            --this.timer;
	            return false;
	         } else {
	            return FoxEntity.this.world.isDay() && this.isAtFavoredLocation() && !this.canCalmDown();
	         }
	      }

	      public void stop() {
	         this.timer = FoxEntity.this.random.nextInt(140);
	         FoxEntity.this.stopActions();
	      }

	      public void start() {
	         FoxEntity.this.setSitting(false);
	         FoxEntity.this.setCrouching(false);
	         FoxEntity.this.setRollingHead(false);
	         FoxEntity.this.setJumping(false);
	         FoxEntity.this.setSleeping(true);
	         FoxEntity.this.getNavigation().stop();
	         FoxEntity.this.getMoveControl().moveTo(FoxEntity.this.getX(), FoxEntity.this.getY(), FoxEntity.this.getZ(), 0.0D);
	      }
	   }

	   abstract class CalmDownGoal extends EntityAIBase  {
	      private final TargetPredicate WORRIABLE_ENTITY_PREDICATE;

	      private CalmDownGoal() {
	         this.WORRIABLE_ENTITY_PREDICATE = (new TargetPredicate()).setBaseMaxDistance(12.0D).includeHidden().setPredicate(FoxEntity.this.new WorriableEntityFilter());
	      }

	      protected boolean isAtFavoredLocation() {
	         BlockPos blockPos = new BlockPos(FoxEntity.this.getX(), FoxEntity.this.getBoundingBox().maxY, FoxEntity.this.getZ());
	         return !FoxEntity.this.world.isSkyVisible(blockPos) && FoxEntity.this.getPathfindingFavor(blockPos) >= 0.0F;
	      }

	      protected boolean canCalmDown() {
	         return !FoxEntity.this.world.getTargets(LivingEntity.class, this.WORRIABLE_ENTITY_PREDICATE, FoxEntity.this, FoxEntity.this.getBoundingBox().expand(12.0D, 6.0D, 12.0D)).isEmpty();
	      }

	      // $FF: synthetic method
	      CalmDownGoal(Object arg) {
	         this();
	      }
	   }

	   public class WorriableEntityFilter implements Predicate {
	      public boolean test(LivingEntity livingEntity) {
	         if (livingEntity instanceof FoxEntity) {
	            return false;
	         } else if (!(livingEntity instanceof ChickenEntity) && !(livingEntity instanceof RabbitEntity) && !(livingEntity instanceof HostileEntity)) {
	            if (livingEntity instanceof TameableEntity) {
	               return !((TameableEntity)livingEntity).isTamed();
	            } else if (livingEntity instanceof PlayerEntity && (livingEntity.isSpectator() || ((PlayerEntity)livingEntity).isCreative())) {
	               return false;
	            } else if (FoxEntity.this.canTrust(livingEntity.getUuid())) {
	               return false;
	            } else {
	               return !livingEntity.isSleeping() && !livingEntity.isSneaky();
	            }
	         } else {
	            return true;
	         }
	      }

	      // $FF: synthetic method
	      public boolean test(Object entity) {
	         return this.test((LivingEntity)entity);
	      }
	   }

	   class AvoidDaylightGoal extends EntityAIRestrictSun {
	      private int timer = 100;

	      public AvoidDaylightGoal(double speed) {
	         super(FoxEntity.this, speed);
	      }

	      public boolean canStart() {
	         if (!FoxEntity.this.isSleeping() && this.mob.getTarget() == null) {
	            if (FoxEntity.this.world.isThundering()) {
	               return true;
	            } else if (this.timer > 0) {
	               --this.timer;
	               return false;
	            } else {
	               this.timer = 100;
	               BlockPos blockPos = this.mob.getBlockPos();
	               return FoxEntity.this.world.isDay() && FoxEntity.this.world.isSkyVisible(blockPos) && !((ServerWorld)FoxEntity.this.world).isNearOccupiedPointOfInterest(blockPos) && this.targetShadedPos();
	            }
	         } else {
	            return false;
	         }
	      }

	      public void start() {
	         FoxEntity.this.stopActions();
	         super.start();
	      }
	   }

	   class DefendFriendGoal extends EntityAINearestAttackableTarget {
	      @Nullable
	      private LivingEntity offender;
	      private LivingEntity friend;
	      private int lastAttackedTime;

	      public DefendFriendGoal(Class targetEntityClass, boolean checkVisibility, boolean checkCanNavigate, @Nullable Predicate<EntityLiving> targetPredicate) {
	         super(FoxEntity.this, targetEntityClass, 10, checkVisibility, checkCanNavigate, targetPredicate);
	      }

	      public boolean canStart() {
	         if (this.reciprocalChance > 0 && this.mob.getRandom().nextInt(this.reciprocalChance) != 0) {
	            return false;
	         } else {
	            Iterator var1 = FoxEntity.this.getTrustedUuids().iterator();

	            while(var1.hasNext()) {
	               UUID uUID = (UUID)var1.next();
	               if (uUID != null && FoxEntity.this.world instanceof ServerWorld) {
	                  Entity entity = ((ServerWorld)FoxEntity.this.world).getEntity(uUID);
	                  if (entity instanceof LivingEntity) {
	                     LivingEntity livingEntity = (LivingEntity)entity;
	                     this.friend = livingEntity;
	                     this.offender = livingEntity.getAttacker();
	                     int i = livingEntity.getLastAttackedTime();
	                     return i != this.lastAttackedTime && this.canTrack(this.offender, this.targetPredicate);
	                  }
	               }
	            }

	            return false;
	         }
	      }

	      public void start() {
	         this.setTargetEntity(this.offender);
	         this.targetEntity = this.offender;
	         if (this.friend != null) {
	            this.lastAttackedTime = this.friend.getLastAttackedTime();
	         }

	         FoxEntity.this.playSound(SoundEvents.ENTITY_FOX_AGGRO, 1.0F, 1.0F);
	         FoxEntity.this.setAggressive(true);
	         FoxEntity.this.stopSleeping();
	         super.start();
	      }
	   }

	   class MateGoal extends EntityAIMate {
	      public MateGoal(double chance) {
	         super(FoxEntity.this, chance);
	      }

	      public void start() {
	         ((FoxEntity)this.animal).stopActions();
	         ((FoxEntity)this.mate).stopActions();
	         super.start();
	      }

	      protected void breed() {
	         ServerWorld serverWorld = (ServerWorld)this.world;
	         FoxEntity foxEntity = (FoxEntity)this.animal.createChild(serverWorld, this.mate);
	         if (foxEntity != null) {
	            ServerPlayerEntity serverPlayerEntity = this.animal.getLovingPlayer();
	            ServerPlayerEntity serverPlayerEntity2 = this.mate.getLovingPlayer();
	            ServerPlayerEntity serverPlayerEntity3 = serverPlayerEntity;
	            if (serverPlayerEntity != null) {
	               foxEntity.addTrustedUuid(serverPlayerEntity.getUuid());
	            } else {
	               serverPlayerEntity3 = serverPlayerEntity2;
	            }

	            if (serverPlayerEntity2 != null && serverPlayerEntity != serverPlayerEntity2) {
	               foxEntity.addTrustedUuid(serverPlayerEntity2.getUuid());
	            }

	            if (serverPlayerEntity3 != null) {
	               serverPlayerEntity3.incrementStat(Stats.ANIMALS_BRED);
	               Criteria.BRED_ANIMALS.trigger(serverPlayerEntity3, this.animal, this.mate, foxEntity);
	            }

	            this.animal.setBreedingAge(6000);
	            this.mate.setBreedingAge(6000);
	            this.animal.resetLoveTicks();
	            this.mate.resetLoveTicks();
	            foxEntity.setBreedingAge(-24000);
	            foxEntity.refreshPositionAndAngles(this.animal.getX(), this.animal.getY(), this.animal.getZ(), 0.0F, 0.0F);
	            serverWorld.spawnEntityAndPassengers(foxEntity);
	            this.world.sendEntityStatus(this.animal, (byte)18);
	            if (this.world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
	               this.world.spawnEntity(new ExperienceOrbEntity(this.world, this.animal.getX(), this.animal.getY(), this.animal.getZ(), this.animal.getRandom().nextInt(7) + 1));
	            }

	         }
	      }
	   }

	   class AttackGoal extends EntityAIAttackOnCollide {
	      public AttackGoal(double speed, boolean pauseWhenIdle) {
	         super(FoxEntity.this, speed, pauseWhenIdle);
	      }

	      protected void attack(LivingEntity target, double squaredDistance) {
	         double d = this.getSquaredMaxAttackDistance(target);
	         if (squaredDistance <= d && this.method_28347()) {
	            this.method_28346();
	            this.mob.tryAttack(target);
	            FoxEntity.this.playSound(SoundEvents.ENTITY_FOX_BITE, 1.0F, 1.0F);
	         }

	      }

	      public void start() {
	         FoxEntity.this.setRollingHead(false);
	         super.start();
	      }

	      public boolean canStart() {
	         return !FoxEntity.this.isSitting() && !FoxEntity.this.isSleeping() && !FoxEntity.this.isInSneakingPose() && !FoxEntity.this.isWalking() && super.canStart();
	      }
	   }

	   class MoveToHuntGoal extends EntityAIBase {
	      public MoveToHuntGoal() {
	         this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
	      }

	      public boolean canStart() {
	         if (FoxEntity.this.isSleeping()) {
	            return false;
	         } else {
	            LivingEntity livingEntity = FoxEntity.this.getTarget();
	            return livingEntity != null && livingEntity.isAlive() && FoxEntity.CHICKEN_AND_RABBIT_FILTER.test(livingEntity) && FoxEntity.this.squaredDistanceTo(livingEntity) > 36.0D && !FoxEntity.this.isInSneakingPose() && !FoxEntity.this.isRollingHead() && !FoxEntity.this.jumping;
	         }
	      }

	      public void start() {
	         FoxEntity.this.setSitting(false);
	         FoxEntity.this.setWalking(false);
	      }

	      public void stop() {
	         LivingEntity livingEntity = FoxEntity.this.getTarget();
	         if (livingEntity != null && FoxEntity.canJumpChase(FoxEntity.this, livingEntity)) {
	            FoxEntity.this.setRollingHead(true);
	            FoxEntity.this.setCrouching(true);
	            FoxEntity.this.getNavigation().stop();
	            FoxEntity.this.getLookControl().lookAt(livingEntity, (float)FoxEntity.this.getBodyYawSpeed(), (float)FoxEntity.this.getLookPitchSpeed());
	         } else {
	            FoxEntity.this.setRollingHead(false);
	            FoxEntity.this.setCrouching(false);
	         }

	      }

	      public void tick() {
	         LivingEntity livingEntity = FoxEntity.this.getTarget();
	         FoxEntity.this.getLookControl().lookAt(livingEntity, (float)FoxEntity.this.getBodyYawSpeed(), (float)FoxEntity.this.getLookPitchSpeed());
	         if (FoxEntity.this.squaredDistanceTo(livingEntity) <= 36.0D) {
	            FoxEntity.this.setRollingHead(true);
	            FoxEntity.this.setCrouching(true);
	            FoxEntity.this.getNavigation().stop();
	         } else {
	            FoxEntity.this.getNavigation().startMovingTo(livingEntity, 1.5D);
	         }

	      }
	   }

	   class FoxMoveControl extends MoveControl {
	      public FoxMoveControl() {
	         super(FoxEntity.this);
	      }

	      public void tick() {
	         if (FoxEntity.this.wantsToPickupItem()) {
	            super.tick();
	         }

	      }
	   }

	   class PickupItemGoal extends EntityAIBase {
	      public PickupItemGoal() {
	         this.setControls(EnumSet.of(Goal.Control.MOVE));
	      }

	      public boolean canStart() {
	         if (!FoxEntity.this.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty()) {
	            return false;
	         } else if (FoxEntity.this.getTarget() == null && FoxEntity.this.getAttacker() == null) {
	            if (!FoxEntity.this.wantsToPickupItem()) {
	               return false;
	            } else if (FoxEntity.this.getRandom().nextInt(10) != 0) {
	               return false;
	            } else {
	               List list = FoxEntity.this.world.getEntitiesByClass(ItemEntity.class, FoxEntity.this.getBoundingBox().expand(8.0D, 8.0D, 8.0D), FoxEntity.PICKABLE_DROP_FILTER);
	               return !list.isEmpty() && FoxEntity.this.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty();
	            }
	         } else {
	            return false;
	         }
	      }

	      public void tick() {
	         List list = FoxEntity.this.world.getEntitiesByClass(ItemEntity.class, FoxEntity.this.getBoundingBox().expand(8.0D, 8.0D, 8.0D), FoxEntity.PICKABLE_DROP_FILTER);
	         ItemStack itemStack = FoxEntity.this.getEquippedStack(EquipmentSlot.MAINHAND);
	         if (itemStack.isEmpty() && !list.isEmpty()) {
	            FoxEntity.this.getNavigation().startMovingTo((Entity)list.get(0), 1.2000000476837158D);
	         }

	      }

	      public void start() {
	         List list = FoxEntity.this.world.getEntitiesByClass(ItemEntity.class, FoxEntity.this.getBoundingBox().expand(8.0D, 8.0D, 8.0D), FoxEntity.PICKABLE_DROP_FILTER);
	         if (!list.isEmpty()) {
	            FoxEntity.this.getNavigation().startMovingTo((Entity)list.get(0), 1.2000000476837158D);
	         }

	      }
	   }
	   
	class AvoidPlayerGoal extends EntityAIAvoidEntity {
		/**
		 * Returns whether the EntityAIBase should begin execution.
		 */
		@Override
		public boolean shouldExecute() {
			if (this.targetEntityClass == EntityPlayer.class) {
				if (this.theEntity instanceof EntityTameable && ((EntityTameable) this.theEntity).isTamed()) {
					return false;
				}

				this.closestLivingEntity = this.theEntity.worldObj.getClosestPlayerToEntity(this.theEntity,
						(double) this.distanceFromEntity);

				if (this.closestLivingEntity == null) {
					return false;
				} else {
					return NOTICEABLE_PLAYER_FILTER.test(closestLivingEntity)
							&& !this.canTrust(closestLivingEntity.getUuid()) && !this.isAggressive();
				}
			}

			Vec3 vec3 = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.theEntity, 16, 7,
					Vec3.createVectorHelper(this.closestLivingEntity.posX, this.closestLivingEntity.posY,
							this.closestLivingEntity.posZ));

			if (vec3 == null) {
				return false;
			} else if (this.closestLivingEntity.getDistanceSq(vec3.xCoord, vec3.yCoord,
					vec3.zCoord) < this.closestLivingEntity.getDistanceSqToEntity(this.theEntity)) {
				return false;
			} else {
				this.entityPathEntity = this.entityPathNavigate.getPathToXYZ(vec3.xCoord, vec3.yCoord, vec3.zCoord);
				return this.entityPathEntity == null ? false : this.entityPathEntity.isDestinationSame(vec3);
			}
		}
	}

	   public static enum Type {
	      RED(0, "red", new RegistryKey[]{BiomeKeys.TAIGA, BiomeKeys.TAIGA_HILLS, BiomeKeys.TAIGA_MOUNTAINS, BiomeKeys.GIANT_TREE_TAIGA, BiomeKeys.GIANT_SPRUCE_TAIGA, BiomeKeys.GIANT_TREE_TAIGA_HILLS, BiomeKeys.GIANT_SPRUCE_TAIGA_HILLS}),
	      SNOW(1, "snow", new RegistryKey[]{BiomeKeys.SNOWY_TAIGA, BiomeKeys.SNOWY_TAIGA_HILLS, BiomeKeys.SNOWY_TAIGA_MOUNTAINS});

	      private static final FoxEntity.Type[] TYPES = (FoxEntity.Type[])Arrays.stream(values()).sorted(Comparator.comparingInt(FoxEntity.Type::getId)).toArray((i) -> {
	         return new FoxEntity.Type[i];
	      });
	      private static final Map NAME_TYPE_MAP = (Map)Arrays.stream(values()).collect(Collectors.toMap(FoxEntity.Type::getKey, (type) -> {
	         return type;
	      }));
	      private final int id;
	      private final String key;
	      private final List biomes;

	      private Type(int id, String key, RegistryKey... registryKeys) {
	         this.id = id;
	         this.key = key;
	         this.biomes = Arrays.asList(registryKeys);
	      }

	      public String getKey() {
	         return this.key;
	      }

	      public int getId() {
	         return this.id;
	      }

	      public static FoxEntity.Type byName(String name) {
	         return (FoxEntity.Type)NAME_TYPE_MAP.getOrDefault(name, RED);
	      }

	      public static FoxEntity.Type fromId(int id) {
	         if (id < 0 || id > TYPES.length) {
	            id = 0;
	         }

	         return TYPES[id];
	      }

	      public static FoxEntity.Type fromBiome(Optional optional) {
	         return optional.isPresent() && SNOW.biomes.contains(optional.get()) ? SNOW : RED;
	      }
	   }
}

