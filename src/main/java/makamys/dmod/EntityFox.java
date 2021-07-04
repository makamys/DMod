package makamys.dmod;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.UUID;

import makamys.dmod.constants.AIMutex;
import makamys.dmod.constants.NBTType;
import makamys.dmod.etfuturum.BlockPos;
import makamys.dmod.future.AnimalEntityEmulator;
import makamys.dmod.future.AnimalEntityFutured;
import makamys.dmod.future.DiveJumpingGoal;
import makamys.dmod.future.EntityAIAttackOnCollideFuture;
import makamys.dmod.future.EntityAIDiveJump;
import makamys.dmod.future.EntityAIFleeSunModern;
import makamys.dmod.future.EntityAIModernAvoidEntity;
import makamys.dmod.future.EntityAnimalFuture;
import makamys.dmod.future.EntityFuture;
import makamys.dmod.future.EntityItemFuture;
import makamys.dmod.future.EntityLivingFutured;
import makamys.dmod.future.EntityPredicates;
import makamys.dmod.future.EntityViewEmulator;
import makamys.dmod.future.ItemStackFuture;
import makamys.dmod.future.MathHelperFuture;
import makamys.dmod.future.ModernEntityLookHelper;
import makamys.dmod.future.PassiveEntityEmulator;
import makamys.dmod.future.TargetPredicate;
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
import net.minecraft.entity.ai.EntityAIFleeSun;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAIRestrictSun;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityLookHelper;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;

public class EntityFox extends EntityAnimalFuture {
		private static final int OWNER = 18;
		private static final int OTHER_TRUSTED = 19;
		private static final int TYPE = 20;
		private static final int FOX_FLAGS = 21;
		/*private static final TrackedData<Integer> TYPE;
		private static final TrackedData<Byte> FOX_FLAGS;
		private static final TrackedData<Optional<UUID>> OWNER;
		private static final TrackedData<Optional<UUID>> OTHER_TRUSTED;*/
	   private static final Predicate<EntityItem> PICKABLE_DROP_FILTER;
	   private static final Predicate<EntityLivingBase> JUST_ATTACKED_SOMETHING_FILTER;
	   private static final Predicate<EntityLivingBase> CHICKEN_AND_RABBIT_FILTER;
	   private static final Predicate<EntityLivingBase> NOTICEABLE_PLAYER_FILTER;
	   private EntityAIBase followChickenAndRabbitTask;
	   /*private Goal followBabyTurtleGoal;
	   private Goal followFishGoal;*/
	   private float headRollProgress;
	   private float lastHeadRollProgress;
	   private float extraRollingHeight;
	   private float lastExtraRollingHeight;
	   private int eatingTime;

	   public EntityFox(World world) {
	      super(world);
	      ReflectionHelper.setPrivateValue(EntityLiving.class, this, new EntityFox.FoxLookHelper(), "lookHelper", "field_70749_g");
	      ReflectionHelper.setPrivateValue(EntityLiving.class, this, new EntityFox.FoxMoveHelper(), "moveHelper", "field_70765_h");
	      //this.setPathfindingPenalty(PathNodeType.DANGER_OTHER, 0.0F);
	      //this.setPathfindingPenalty(PathNodeType.DAMAGE_OTHER, 0.0F);
	      this.setSize(0.6F, 0.7F);
	      this.setCanPickUpLoot(true);
	      initTasks();
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
	   protected void initTasks() {
		    this.followChickenAndRabbitTask = new EntityAINearestAttackableTarget(this, EntityLiving.class, 10, false, false, (livingEntity) -> {
		    	return livingEntity instanceof EntityChicken || ConfigDMod.rabbitEntities.contains(livingEntity.getClass());
		    });
	      /*this.followBabyTurtleGoal = new FollowTargetGoal(this, TurtleEntity.class, 10, false, false, TurtleEntity.BABY_TURTLE_ON_LAND_FILTER);
	      this.followFishGoal = new FollowTargetGoal(this, FishEntity.class, 20, false, false, (livingEntity) -> {
	         return livingEntity instanceof SchoolingFishEntity;
	      });*/
	      this.tasks.addTask(0, new EntityFox.AISwim());
		  this.tasks.addTask(1, new EntityFox.AIStopWandering());
		  this.tasks.addTask(2, new EntityFox.AIEscapeWhenNotAggressive(2.2D));
		  this.tasks.addTask(3, new EntityFox.AIMate(1.0D));
	      this.tasks.addTask(4, new EntityAIModernAvoidEntity(this, EntityPlayer.class, 16.0F, 1.6D, 1.4D, (livingEntity) -> {
	         return NOTICEABLE_PLAYER_FILTER.test(livingEntity) && !this.canTrust(livingEntity.getUniqueID()) && !this.isAggressive();
	      }));
	      this.tasks.addTask(4, new EntityAIModernAvoidEntity(this, EntityWolf.class, 8.0F, 1.6D, 1.4D, (livingEntity) -> {
	         return !((EntityWolf)livingEntity).isTamed() && !this.isAggressive();
	      }));
	      /*this.tasks.addTask(4, new EntityAIModernAvoidEntity(this, PolarBearEntity.class, 8.0F, 1.6D, 1.4D, (livingEntity) -> {
	         return !this.isAggressive();
	      }));*/
	    this.tasks.addTask(5, new EntityFox.AIMoveToHunt());
	    this.tasks.addTask(6, new EntityFox.AIJumpChase());
	    this.tasks.addTask(6, new EntityFox.AIAvoidDaylight(1.25D));
	    this.tasks.addTask(7, new EntityFox.AIAttack(1.2000000476837158D, true));
	    this.tasks.addTask(7, new EntityFox.AIDelayedCalmDown());
	    this.tasks.addTask(8, new EntityFox.AIFollowParent(this, 1.25D));
	      //this.tasks.addTask(9, new EntityFox.GoToVillageGoal(32, 200));
	      // TODO
	      //this.tasks.addTask(10, new EntityFox.EatSweetBerriesGoal(1.2000000476837158D, 12, 2));
	      this.tasks.addTask(10, new EntityAILeapAtTarget(this, 0.4F));
	      this.tasks.addTask(11, new EntityAIWander(this, 1.0D));
	    //XXXthis.tasks.addTask(11, new EntityFox.PickupItemGoal());
	    this.tasks.addTask(12, new EntityFox.AILookAtEntity(this, EntityPlayer.class, 24.0F));
	    this.tasks.addTask(13, new EntityFox.AISitDownAndLookAround());
	    //XXXthis.targetTasks.addTask(3, new EntityFox.DefendFriendGoal(EntityLiving.class, false, false, (livingEntity) -> {
	    //XXXreturn JUST_ATTACKED_SOMETHING_FILTER.test(livingEntity) && !this.canTrust(livingEntity.getUniqueID());
	       //XXX}));
	   }

	   @Override
	   public String getEatSound(ItemStack stack) {
	      return DMod.MODID + ":entity.fox.eat";
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
	               } else {
	            	   this.setCurrentItemOrArmor(0, null);
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

	      if (this.isPlayerSleeping() || this.isImmobile()) {
	         this.isJumping = false;
	         this.moveStrafing = 0.0F;
	         this.moveForward = 0.0F;
	      }

	      super.onLivingUpdate();
	      if (this.isAggressive() && this.rand.nextFloat() < 0.05F) {
	         this.playSound(DMod.MODID + ":entity.fox.aggro", 1.0F, 1.0F); //TODO
	      }

	   }
	   
	   // XXX not called
	   protected boolean isImmobile() {
	      return this.isDead;
	   }

	   private boolean canEat(ItemStack stack) {
	      return stack != null && stack.getItem() instanceof ItemFood && this.getAttackTarget() == null && this.onGround && !this.isPlayerSleeping();
	   }

	   // TODO it would be nice to have this configurable
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
					Vec3 rv = EntityFuture.getRotationVector(this);
					this.worldObj.spawnParticle(DUtil.getItemStackParticleName(itemStack),
							this.posX + rv.xCoord / 2.0D, this.posY,
							this.posZ + rv.zCoord / 2.0D, vec3d.xCoord, vec3d.yCoord + 0.05D, vec3d.zCoord);
				}
			}
		} else {
			super.handleHealthUpdate(status);
		}

	}

		protected void applyEntityAttributes() {
			super.applyEntityAttributes();
			this.getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage);
			this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(10.0D);
			this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.30000001192092896D);
			this.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(32D);
			this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(2D);
		}
	   
	   public EntityFox createChild(EntityAgeable passiveEntity) {
	      EntityFox EntityFox = new EntityFox(this.worldObj);
	      EntityFox.setType(this.rand.nextBoolean() ? this.getFoxType() : ((EntityFox)passiveEntity).getFoxType());
	      return EntityFox;
	   }

	   public IEntityLivingData onSpawnWithEgg(IEntityLivingData entityData){
		   entityData = super.onSpawnWithEgg(entityData);
	      Optional<BiomeGenBase> optional = Optional.of(worldObj.getBiomeGenForCoords((int)this.posX, (int)this.posZ));
	      EntityFox.Type type = EntityFox.Type.fromBiome(optional);
	      boolean bl = false;
	      if (entityData instanceof EntityFox.FoxData) {
	         type = ((EntityFox.FoxData)entityData).type;
	         if (((EntityFox.FoxData)entityData).getSpawnedCount() >= 2) {
	            bl = true;
	         }
	      } else {
	         entityData = new EntityFox.FoxData(type);
	      }

	      this.setType(type);
	      if (bl) {
	         this.setGrowingAge(-24000);
	      }

	      if (worldObj instanceof WorldServer) {
	         this.addTypeSpecificTasks();
	      }

	      this.initEquipment(/*difficulty*/);
	      return PassiveEntityEmulator.postOnSpawnWithEgg(this, entityData, rand);
	   }

	   private void addTypeSpecificTasks() {
	      if (this.getFoxType() == EntityFox.Type.RED) {
	         this.targetTasks.addTask(4, this.followChickenAndRabbitTask);
	         //this.targetTasks.addTask(4, this.followBabyTurtleGoal);
	         //this.targetTasks.addTask(6, this.followFishGoal);
	      } else {
	         //this.targetTasks.addTask(4, this.followFishGoal);
	         this.targetTasks.addTask(6, this.followChickenAndRabbitTask);
	         //this.targetTasks.addTask(6, this.followBabyTurtleGoal);
	      }
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

	   public EntityFox.Type getFoxType() {
	      return EntityFox.Type.fromId(dataWatcher.getWatchableObjectByte(TYPE));
	   }

	   private void setType(EntityFox.Type type) {
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
		tag.setBoolean("Sleeping", this.isPlayerSleeping());
		tag.setString("Type", this.getFoxType().getKey());
		tag.setBoolean("Sitting", this.isSitting());
		tag.setBoolean("Crouching", this.isInSneakingPose());
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	public void readEntityFromNBT(NBTTagCompound tag) {
		super.readEntityFromNBT(tag);
		NBTTagList listTag = tag.getTagList("Trusted", NBTType.STRING);
		
		for(int i = 0; i < listTag.tagCount(); ++i) {
	         this.addTrustedUuid(UUID.fromString(listTag.getStringTagAt(i)));
	      }

	      this.setSleeping(tag.getBoolean("Sleeping"));
	      this.setType(EntityFox.Type.byName(tag.getString("Type")));
	      this.setSitting(tag.getBoolean("Sitting"));
	      this.setCrouching(tag.getBoolean("Crouching"));
	      if (this.worldObj instanceof WorldServer) {
	         this.addTypeSpecificTasks();
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

	   @Override
	   public boolean isPlayerSleeping() {
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
	      ItemStack itemStack = this.getEquipmentInSlot(0);
	      return itemStack == null || this.eatingTime > 0 && item instanceof ItemFood && !(itemStack.getItem() instanceof ItemFood);
	   }

	   private void spit(ItemStack stack) {
	      if (stack != null && !this.worldObj.isRemote) {
	    	  Vec3 rv = EntityFuture.getRotationVector(this);
	         EntityItem EntityItem = new EntityItem(this.worldObj, this.posX + rv.xCoord, this.posY + 1.0D, this.posZ + rv.zCoord, stack);
	         EntityItem.delayBeforeCanPickup = 40;
	         EntityItem.func_145799_b(getUniqueID().toString()); // is this OK?
	         this.playSound(DMod.MODID + ":entity.fox.spit", 1.0F, 1.0F); // TODO
	         this.worldObj.spawnEntityInWorld(EntityItem);
	      }
	   }

	   private void dropItem(ItemStack stack) {
	      EntityItem EntityItem = new EntityItem(this.worldObj, this.posX, this.posY, this.posZ, stack);
	      this.worldObj.spawnEntityInWorld(EntityItem);
	   }

	   protected void loot(EntityItem item) {
	      ItemStack itemStack = item.getEntityItem();
	      if (this.canPickupItem(itemStack)) {
	         int i = itemStack.stackSize;
	         if (i > 1) {
	            this.dropItem(itemStack.splitStack(i - 1));
	         }

	         this.spit(this.getEquipmentInSlot(0));
	         // this.method_29499(item); // advancement stuff
	         this.setCurrentItemOrArmor(0, itemStack.splitStack(1));
	         this.equipmentDropChances[0] = 2.0F;
	         this.onItemPickup(item, itemStack.stackSize);
	         item.setDead();
	         this.eatingTime = 0;
	      }

	   }

	   public void onUpdate() {
	      super.onUpdate();
	      if (this.worldObj.isRemote) {
	         boolean bl = this.inWater;
	         if (bl || this.getAttackTarget() != null || this.worldObj.isThundering()) {
	            this.stopSleeping();
	         }

	         if (bl || this.isPlayerSleeping()) {
	            this.setSitting(false);
	         }

	         if (this.isWalking() && this.worldObj.rand.nextFloat() < 0.2F) {
	            this.worldObj.playAuxSFX(2001, 
	            		MathHelper.floor_double(posX),
	            		MathHelper.floor_double(posY),
	            		MathHelper.floor_double(posZ),
	            		worldObj.getBlockMetadata(MathHelper.floor_double(posX),
	            				MathHelper.floor_double(posY),
	            				MathHelper.floor_double(posZ)));
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
	      return ConfigDMod.foxBreedingItems.contains(stack.getItem());
	   }
/*
	   protected void onPlayerSpawnedChild(PlayerEntity player, MobEntity child) {
	      ((EntityFox)child).addTrustedUuid(player.getUuid());
	   }
*/
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

	   @SideOnly(Side.CLIENT)
	   public float getHeadRoll(float tickDelta) {
	      return MathHelperFuture.lerp(tickDelta, this.lastHeadRollProgress, this.headRollProgress) * 0.11F * 3.1415927F;
	   }

	   @SideOnly(Side.CLIENT)
	   public float getBodyRotationHeightOffset(float tickDelta) {
	      return MathHelperFuture.lerp(tickDelta, this.lastExtraRollingHeight, this.extraRollingHeight);
	   }
	   
	   // XXX setTarget? setAttackTarget? what's the difference?
	   public void setAttackTarget(EntityLivingBase target) {
	      if (this.isAggressive() && target == null) {
	         this.setAggressive(false);
	      }

	      super.setAttackTarget(target);
	   }
	   
	   @Override
	   public float computeFallDistance(float fallDistance) {
		   return fallDistance - 2f;
	   }
	   
	   private void stopSleeping() {
	      this.setSleeping(false);
	   }
	   
	    public boolean isAIEnabled()
	    {
	        return true;
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
	      return !this.isPlayerSleeping() && !this.isSitting() && !this.isWalking();
	   }

	   public void playLivingSound() {
	      String soundEvent = this.getLivingSound();
	      if (soundEvent.equals(DMod.MODID + ":entity.fox.screech")) {
	         this.playSound(soundEvent, 2.0F, this.getSoundPitch());
	      } else {
	         super.playLivingSound();
	      }
	   }

	   protected String getLivingSound() {
	      if (this.isPlayerSleeping()) {
	         return DMod.MODID + ":entity.fox.sleep";
	      } else {
	         if (!this.worldObj.isDaytime() && this.rand.nextFloat() < 0.1F) {
	            List list = this.worldObj.getEntitiesWithinAABB(EntityPlayer.class, this.boundingBox.expand(16.0D, 16.0D, 16.0D));
	            if (list.isEmpty()) {
	               return DMod.MODID + ":entity.fox.screech";
	            }
	         }

	         return DMod.MODID + ":entity.fox.ambient";
	      }
	   }

	   protected String getHurtSound() {
	      return DMod.MODID + ":entity.fox.hurt";
	   }

	   protected String getDeathSound() {
	      return DMod.MODID + ":entity.fox.death";
	   }

	   private boolean canTrust(UUID uuid) {
	      return this.getTrustedUuids().contains(uuid);
	   }

	   public void onDeath(DamageSource source) {
		   if(!this.worldObj.isRemote) {
		      ItemStack itemStack = this.getEquipmentInSlot(0);
		      if (itemStack != null) {
		    	 this.entityDropItem(itemStack, 0.0F);
		         this.setCurrentItemOrArmor(0, null);
		      }
		   }

	      super.onDeath(source);
	   }

	   public static boolean canJumpChase(EntityFox fox, EntityLivingBase chasedEntity) {
	      double d = chasedEntity.posZ - fox.posZ;
	      double e = chasedEntity.posX - fox.posX;
	      double f = d / e;
	      //int i = true;

	      for(int j = 0; j < 6; ++j) {
	         double g = f == 0.0D ? 0.0D : d * (double)((float)j / 6.0F);
	         double h = f == 0.0D ? e * (double)((float)j / 6.0F) : g / f;

	         for(int k = 1; k < 4; ++k) {
	            if (!fox.worldObj.getBlock(
	            		MathHelper.floor_double(fox.posX + h),
	            		MathHelper.floor_double(fox.posY + (double)k), 
	            		MathHelper.floor_double(fox.posZ + g)).getMaterial().isReplaceable()) {
	               return false;
	            }
	         }
	      }

	      return true;
	   }
	   
	   @Override
		public boolean attackEntityAsMob(Entity p_70652_1_) {
			boolean result = super.attackEntityAsMob(p_70652_1_);
			if(result) {
				EntityFox.this.playSound(DMod.MODID + ":entity.fox.bite", 1.0F, 1.0F);
			}
			return result;
		}
	   
	   // TODO
/*
	   @SideOnly(Side.CLIENT)
	   public Vec3d method_29919() {
	      return new Vec3d(0.0D, (double)(0.55F * this.getStandingEyeHeight()), (double)(this.getWidth() * 0.4F));
	   }
*/

	   static {
	      /*TYPE = DataTracker.registerData(EntityFox.class, TrackedDataHandlerRegistry.INTEGER);
	      FOX_FLAGS = DataTracker.registerData(EntityFox.class, TrackedDataHandlerRegistry.BYTE);
	      OWNER = DataTracker.registerData(EntityFox.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
	      OTHER_TRUSTED = DataTracker.registerData(EntityFox.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);*/
	      PICKABLE_DROP_FILTER = (EntityItem) -> {
	         return !EntityItemFuture.cannotPickUp(EntityItem) && EntityItem.isEntityAlive();
	      };
	      JUST_ATTACKED_SOMETHING_FILTER = (entity) -> {
	         if (!(entity instanceof EntityLiving)) {
	            return false;
	         } else {
	            EntityLiving livingEntity = (EntityLiving)entity;
	            return livingEntity.getLastAttacker() != null && livingEntity.getLastAttackerTime() < livingEntity.ticksExisted + 600;
	         }
	      };
	      CHICKEN_AND_RABBIT_FILTER = (entity) -> {
	         return entity instanceof EntityChicken || ConfigDMod.rabbitEntities.contains(entity.getClass());
	      };
	      NOTICEABLE_PLAYER_FILTER = (entity) -> {
	         return !entity.isSneaking() && EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.test(entity);
	      };
	   }

	   class AILookAtEntity extends EntityAIWatchClosest {
	      public AILookAtEntity(EntityLiving fox, Class targetType, float range) {
	         super(fox, targetType, range);
	      }

	      @Override
	      public boolean shouldExecute() {
	         return super.shouldExecute() && !EntityFox.this.isWalking() && !EntityFox.this.isRollingHead();
	      }

	      @Override
	      public boolean continueExecuting() {
	         return super.continueExecuting() && !EntityFox.this.isWalking() && !EntityFox.this.isRollingHead();
	      }
	   }

	   class AIFollowParent extends EntityAIFollowParent {
	      private final EntityFox fox;

	      public AIFollowParent(EntityFox fox, double speed) {
	         super(fox, speed);
	         this.fox = fox;
	      }

	      @Override
	      public boolean shouldExecute() {
	         return !this.fox.isAggressive() && super.shouldExecute();
	      }

	      @Override
	      public boolean continueExecuting() {
	         return !this.fox.isAggressive() && super.continueExecuting();
	      }

	      @Override
	      public void startExecuting() {
	         this.fox.stopActions();
	         super.startExecuting();
	      }
	   }

	   public class FoxLookHelper extends ModernEntityLookHelper {
	      public FoxLookHelper() {
	         super(EntityFox.this);
	      }

	      @Override
	      public void onUpdateLook() {
	         if (!EntityFox.this.isPlayerSleeping()) {
	            super.onUpdateLook();
	         }
	      }

	      @Override
	      protected boolean shouldStayHorizontal() {
	         return !EntityFox.this.isChasing() && !EntityFox.this.isInSneakingPose() && !EntityFox.this.isRollingHead() & !EntityFox.this.isWalking();
	      }
	   }

	   public class AIJumpChase extends EntityAIDiveJump {
		   @Override
	      public boolean shouldExecute() {
	         if (!EntityFox.this.isFullyCrouched()) {
	            return false;
	         } else {
	            EntityLivingBase livingEntity = EntityFox.this.getAttackTarget();
	            if (livingEntity != null && livingEntity.isEntityAlive()) {
	               if (EntityFuture.getMovementDirection(livingEntity) != EntityFuture.getHorizontalFacing(livingEntity)) {
	                  return false;
	               } else {
	                  boolean bl = EntityFox.canJumpChase(EntityFox.this, livingEntity);
	                  if (!bl) {
	                     EntityFox.this.getNavigator().tryMoveToEntityLiving((Entity)livingEntity, 0);
	                     EntityFox.this.setCrouching(false);
	                     EntityFox.this.setRollingHead(false);
	                  }

	                  return bl;
	               }
	            } else {
	               return false;
	            }
	         }
	      }

	      @Override
	      public boolean continueExecuting() {
	         EntityLivingBase livingEntity = EntityFox.this.getAttackTarget();
	         if (livingEntity != null && livingEntity.isEntityAlive()) {
	            double d = EntityFox.this.motionY;
	            return (d * d >= 0.05000000074505806D || Math.abs(EntityFox.this.rotationPitch) >= 15.0F || !EntityFox.this.onGround) && !EntityFox.this.isWalking();
	         } else {
	            return false;
	         }
	      }

	      @Override
	      public boolean isInterruptible() {
	         return false;
	      }

	      @Override
	      public void startExecuting() {
	         EntityFox.this.setJumping(true);
	         EntityFox.this.setChasing(true);
	         EntityFox.this.setRollingHead(false);
	         EntityLivingBase livingEntity = EntityFox.this.getAttackTarget();
	         EntityFox.this.getLookHelper().setLookPositionWithEntity(livingEntity, 60.0F, 30.0F);
	         Vec3 vec3d = Vec3.createVectorHelper(livingEntity.posX - EntityFox.this.posX, livingEntity.posY - EntityFox.this.posY, livingEntity.posZ - EntityFox.this.posZ).normalize();
	         EntityFuture.setVelocity(EntityFox.this, EntityFuture.getVelocity(EntityFox.this).addVector(vec3d.xCoord * 0.8D, 0.9D, vec3d.zCoord * 0.8D));
	         EntityFox.this.getNavigator().clearPathEntity();
	      }

	      @Override
	      public void resetTask() {
	         EntityFox.this.setCrouching(false);
	         EntityFox.this.extraRollingHeight = 0.0F;
	         EntityFox.this.lastExtraRollingHeight = 0.0F;
	         EntityFox.this.setRollingHead(false);
	         EntityFox.this.setChasing(false);
	      }

	      @Override
	      public void updateTask() {
	         EntityLivingBase livingEntity = EntityFox.this.getAttackTarget();
	         if (livingEntity != null) {
	            EntityFox.this.getLookHelper().setLookPositionWithEntity(livingEntity, 60.0F, 30.0F);
	         }

	         if (!EntityFox.this.isWalking()) {
	            Vec3 vec3d = EntityFuture.getVelocity(EntityFox.this);
	            if (vec3d.yCoord * vec3d.yCoord < 0.029999999329447746D && EntityFox.this.rotationPitch != 0.0F) {
	               EntityFox.this.rotationPitch = MathHelperFuture.lerpAngle(EntityFox.this.rotationPitch, 0.0F, 0.2F);
	            } else {
	               double d = Math.sqrt(EntityFuture.squaredHorizontalLength(vec3d));
	               double e = Math.signum(-vec3d.yCoord) * Math.acos(d / vec3d.lengthVector()) * 57.2957763671875D;
	               EntityFox.this.rotationPitch = (float)e;
	            }
	         }

	         if (livingEntity != null && EntityFox.this.getDistanceToEntity(livingEntity) <= 2.0F) {
	            EntityFox.this.attackEntityAsMob(livingEntity);
	         } else if (EntityFox.this.rotationPitch > 0.0F && EntityFox.this.onGround && (float)EntityFox.this.motionY != 0.0F && EntityFox.this.worldObj.getBlock(
	        		 MathHelper.floor_double(posX),
          		   MathHelper.floor_double(posY),
          		   MathHelper.floor_double(posZ)) == Blocks.snow_layer) {
	            EntityFox.this.rotationPitch = 60.0F;
	            EntityFox.this.setAttackTarget(null);
	            EntityFox.this.setWalking(true);
	         }

	      }
	   }

	   class AISwim extends EntityAISwimming {
	      public AISwim() {
	         super(EntityFox.this);
	      }
	      
	      @Override
	      public void startExecuting() {
	         super.startExecuting();
	         EntityFox.this.stopActions();
	      }
	      
	      @Override
	      public boolean shouldExecute() {
	    	  return super.shouldExecute();
	      }
	   }

	   /*class GoToVillageGoal extends net.minecraft.entity.ai.goal.GoToVillageGoal {
	      public GoToVillageGoal(int unused, int searchRange) {
	         super(EntityFox.this, searchRange);
	      }

	      public void start() {
	         EntityFox.this.stopActions();
	         super.start();
	      }

	      public boolean canStart() {
	         return super.canStart() && this.canGoToVillage();
	      }

	      public boolean shouldContinue() {
	         return super.shouldContinue() && this.canGoToVillage();
	      }

	      private boolean canGoToVillage() {
	         return !EntityFox.this.isPlayerSleeping() && !EntityFox.this.isSitting() && !EntityFox.this.isAggressive() && EntityFox.this.getAttackTarget() == null;
	      }
	   }*/

	   class AIEscapeWhenNotAggressive extends EntityAIPanic {
	      public AIEscapeWhenNotAggressive(double speed) {
	         super(EntityFox.this, speed);
	      }
	      
	      @Override
	      public boolean shouldExecute() {
	         return !EntityFox.this.isAggressive() && super.shouldExecute();
	      }
	   }

	   class AIStopWandering extends EntityAIBase {
	      int timer;

	      public AIStopWandering() {
	    	  setMutexBits(AIMutex.LOOK | AIMutex.JUMP | AIMutex.MOVE);
	      }

	      @Override
	      public boolean shouldExecute() {
	         return EntityFox.this.isWalking();
	      }

	      @Override
	      public boolean continueExecuting() {
	         return this.shouldExecute() && this.timer > 0;
	      }

	      @Override
	      public void startExecuting() {
	         this.timer = 40;
	      }

	      @Override
	      public void resetTask() {
	         EntityFox.this.setWalking(false);
	      }
	      
	      @Override
	      public void updateTask() {
	         --this.timer;
	      }
	   }
	   
	   // implement IEntityAdditionalSpawnData?
	   public static class FoxData extends PassiveEntityEmulator.PassiveData {
	      public final EntityFox.Type type;

	      public FoxData(EntityFox.Type type) {
	    	  super(false);
	         this.type = type;
	      }
	   }
/*
	   public class EatSweetBerriesGoal extends MoveToTargetPosGoal {
	      protected int timer;

	      public EatSweetBerriesGoal(double speed, int range, int maxYDifference) {
	         super(EntityFox.this, speed, range, maxYDifference);
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
	         } else if (!this.hasReached() && EntityFox.this.rand.nextFloat() < 0.05F) {
	            EntityFox.this.playSound(DMod.MODID + ":entity.fox.sniff", 1.0F, 1.0F);
	         }

	         super.tick();
	      }

	      protected void eatSweetBerry() {
	         if (EntityFox.this.worldObj.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
	            BlockState blockState = EntityFox.this.worldObj.getBlockState(this.targetPos);
	            if (blockState.isOf(Blocks.SWEET_BERRY_BUSH)) {
	               int i = (Integer)blockState.get(SweetBerryBushBlock.AGE);
	               blockState.with(SweetBerryBushBlock.AGE, 1);
	               int j = 1 + EntityFox.this.worldObj.rand.nextInt(2) + (i == 3 ? 1 : 0);
	               ItemStack itemStack = EntityFox.this.getEquippedStack(EquipmentSlot.MAINHAND);
	               if (itemStack.isEmpty()) {
	                  EntityFox.this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.SWEET_BERRIES));
	                  --j;
	               }

	               if (j > 0) {
	                  Block.dropStack(EntityFox.this.worldObj, this.targetPos, new ItemStack(Items.SWEET_BERRIES, j));
	               }

	               EntityFox.this.playSound("item.sweet_berries.pick_from_bush", 1.0F, 1.0F);
	               EntityFox.this.worldObj.setBlockState(this.targetPos, (BlockState)blockState.with(SweetBerryBushBlock.AGE, 1), 2);
	            }
	         }
	      }

	      public boolean canStart() {
	         return !EntityFox.this.isPlayerSleeping() && super.canStart();
	      }

	      public void start() {
	         this.timer = 0;
	         EntityFox.this.setSitting(false);
	         super.start();
	      }
	   }
*/
	   class AISitDownAndLookAround extends EntityFox.AICalmDown {
	      private double lookX;
	      private double lookZ;
	      private int timer;
	      private int counter;

	      public AISitDownAndLookAround() {
	         super(null);
	         this.setMutexBits(AIMutex.MOVE | AIMutex.LOOK);
	      }

	      @Override
	      public boolean shouldExecute() {
	         return EntityFox.this.getEntityToAttack() == null && EntityFox.this.rand.nextFloat() < 0.02F && !EntityFox.this.isPlayerSleeping() && EntityFox.this.getAttackTarget() == null && EntityFox.this.getNavigator().noPath() && !this.canNotCalmDown() && !EntityFox.this.isChasing() && !EntityFox.this.isInSneakingPose();
	      }

	      @Override
	      public boolean continueExecuting() {
	         return this.counter > 0;
	      }

	      @Override
	      public void startExecuting() {
	         this.chooseNewAngle();
	         this.counter = 2 + EntityFox.this.rand.nextInt(3);
	         EntityFox.this.setSitting(true);
	         EntityFox.this.getNavigator().clearPathEntity();
	      }
	      
	      @Override
	      public void resetTask() {
	         EntityFox.this.setSitting(false);
	      }

	      @Override
	      public void updateTask() {
	         --this.timer;
	         if (this.timer <= 0) {
	            --this.counter;
	            this.chooseNewAngle();
	         }

	         EntityFox.this.getLookHelper().setLookPosition(EntityFox.this.posX + this.lookX, EntityFox.this.posY + EntityFox.this.getEyeHeight(), EntityFox.this.posZ + this.lookZ, (float)EntityFox.this.getBodyYawSpeed(), (float)EntityFox.this.getLookPitchSpeed());
	      }

	      private void chooseNewAngle() {
	         double d = 6.283185307179586D * EntityFox.this.rand.nextDouble();
	         this.lookX = Math.cos(d);
	         this.lookZ = Math.sin(d);
	         this.timer = 80 + EntityFox.this.rand.nextInt(20);
	      }
	   }

	   class AIDelayedCalmDown extends EntityFox.AICalmDown {
	      private int timer;

	      public AIDelayedCalmDown() {
	         super(null);
	         this.timer = EntityFox.this.rand.nextInt(140);
	         this.setMutexBits(AIMutex.MOVE | AIMutex.LOOK | AIMutex.JUMP);
	      }

	      @Override
	      public boolean shouldExecute() {
	         if (EntityFox.this.moveStrafing == 0.0F && EntityFox.this.onGround && EntityFox.this.moveForward == 0.0F) {
	            return this.canCalmDown() || EntityFox.this.isPlayerSleeping();
	         } else {
	            return false;
	         }
	      }

	      @Override
	      public boolean continueExecuting() {
	         return this.canCalmDown();
	      }

	      private boolean canCalmDown() {
	         if (this.timer > 0) {
	            --this.timer;
	            return false;
	         } else {
	            return EntityFox.this.worldObj.isDaytime() && this.isAtFavoredLocation() && !this.canNotCalmDown();
	         }
	      }

	      @Override
	      public void resetTask() {
	         this.timer = EntityFox.this.rand.nextInt(140);
	         EntityFox.this.stopActions();
	      }

	      @Override
	      public void startExecuting() {
	         EntityFox.this.setSitting(false);
	         EntityFox.this.setCrouching(false);
	         EntityFox.this.setRollingHead(false);
	         EntityFox.this.setJumping(false);
	         EntityFox.this.setSleeping(true);
	         EntityFox.this.getNavigator().clearPathEntity();
	         EntityFox.this.getMoveHelper().setMoveTo(EntityFox.this.posX, EntityFox.this.posY, EntityFox.this.posZ, 0.0D);
	      }
	   }

	   abstract class AICalmDown extends EntityAIBase  {
	      private final TargetPredicate WORRIABLE_ENTITY_PREDICATE;

	      private AICalmDown() {
	         this.WORRIABLE_ENTITY_PREDICATE = (new TargetPredicate()).setBaseMaxDistance(12.0D).includeHidden().setPredicate(EntityFox.this.new WorriableEntityFilter());
	      }

	      protected boolean isAtFavoredLocation() {
	         BlockPos blockPos = new BlockPos(EntityFox.this.posX, EntityFox.this.boundingBox.maxY, EntityFox.this.posZ);
	         return !EntityFox.this.worldObj.canBlockSeeTheSky(blockPos.getX(), blockPos.getY(), blockPos.getZ()) &&
	        		 EntityFox.this.getBlockPathWeight(blockPos.getX(), blockPos.getY(), blockPos.getZ()) >= 0.0F;
	      }

	      protected boolean canNotCalmDown() {
	    	  //return !F.worldObj.selectEntitiesWithinAABB(EntityLivingBase.class, this.boundingBox.expand(20.0D, 8.0D, 20.0D), attackEntitySelector);
	         return !EntityViewEmulator.getTargets(EntityFox.this.worldObj, EntityLivingBase.class, this.WORRIABLE_ENTITY_PREDICATE, EntityFox.this, EntityFox.this.boundingBox.expand(12.0D, 6.0D, 12.0D)).isEmpty();
	      }

	      // $FF: synthetic method
	      AICalmDown(Object arg) {
	         this();
	      }
	   }

	   public class WorriableEntityFilter implements Predicate<EntityLivingBase> {
	      public boolean test(EntityLivingBase livingEntity) {
	         if (livingEntity instanceof EntityFox) {
	            return false;
	         } else if (!(livingEntity instanceof EntityChicken) && !ConfigDMod.rabbitEntities.contains(livingEntity.getClass()) && !(livingEntity instanceof EntityMob)) {
	            if (livingEntity instanceof EntityTameable) {
	               return !((EntityTameable)livingEntity).isTamed();
	            } else if (livingEntity instanceof EntityPlayer && (/*livingEntity.isSpectator() || */((EntityPlayer)livingEntity).capabilities.isCreativeMode)) {
	               return false;
	            } else if (EntityFox.this.canTrust(livingEntity.getUniqueID())) {
	               return false;
	            } else {
	               return !livingEntity.isPlayerSleeping() && !livingEntity.isSneaking();
	            }
	         } else {
	            return true;
	         }
	      }
/*
	      // $FF: synthetic method
	      public boolean test(Object entity) {
	         return this.test((LivingEntity)entity);
	      }*/
	   }

	   class AIAvoidDaylight extends EntityAIFleeSunModern {
	      private int timer = 100;

	      public AIAvoidDaylight(double speed) {
	         super(EntityFox.this, speed);
	      }

	      @Override
	      public boolean shouldExecute() {
	         if (!EntityFox.this.isPlayerSleeping() && EntityFox.this.getAttackTarget() == null) {
	            if (EntityFox.this.worldObj.isThundering()) {
	               return EntityFox.this.worldObj.canBlockSeeTheSky(
	            		   MathHelper.floor_double(posX),
	            		   MathHelper.floor_double(posY),
	            		   MathHelper.floor_double(posZ)) /*&& !((WorldServer)EntityFox.this.worldObj).isNearOccupiedPointOfInterest(blockPos)*/
	            		   && this.targetShadedPos();
	            } else if (this.timer > 0) {
	               --this.timer;
	               return false;
	            } else {
	               this.timer = 100;
	               return EntityFox.this.worldObj.isDaytime() && EntityFox.this.worldObj.canBlockSeeTheSky(
	            		   MathHelper.floor_double(posX),
	            		   MathHelper.floor_double(posY),
	            		   MathHelper.floor_double(posZ)) /*&& !((WorldServer)EntityFox.this.worldObj).isNearOccupiedPointOfInterest(blockPos)*/
	            		   && this.targetShadedPos();
	            }
	         } else {
	            return false;
	         }
	      }

	      @Override
	      public void startExecuting() {
	         EntityFox.this.stopActions();
	         super.startExecuting();
	      }
	   }

/*	   class DefendFriendGoal extends EntityAINearestAttackableTarget {
	      @Nullable
	      private LivingEntity offender;
	      private LivingEntity friend;
	      private int lastAttackedTime;

	      public DefendFriendGoal(Class targetEntityClass, boolean checkVisibility, boolean checkCanNavigate, @Nullable Predicate<EntityLiving> targetPredicate) {
	         super(EntityFox.this, targetEntityClass, 10, checkVisibility, checkCanNavigate, targetPredicate);
	      }

	      public boolean canStart() {
	         if (this.reciprocalChance > 0 && this.mob.rand.nextInt(this.reciprocalChance) != 0) {
	            return false;
	         } else {
	            Iterator var1 = EntityFox.this.getTrustedUuids().iterator();

	            while(var1.hasNext()) {
	               UUID uUID = (UUID)var1.next();
	               if (uUID != null && EntityFox.this.world instanceof ServerWorld) {
	                  Entity entity = ((ServerWorld)EntityFox.this.world).getEntity(uUID);
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

	         EntityFox.this.playSound(entity.fox.aggro, 1.0F, 1.0F);
	         EntityFox.this.setAggressive(true);
	         EntityFox.this.stopSleeping();
	         super.start();
	      }
	   }*/

	   class AIMate extends EntityAIMate {
	      public AIMate(double chance) {
	         super(EntityFox.this, chance);
	      }

	      @Override
	      public void startExecuting() {
	         ((EntityFox)this.theAnimal).stopActions();
	         ((EntityFox)this.targetMate).stopActions();
	         super.startExecuting();
	      }

	      @Override
	      protected void spawnBaby() {
	         WorldServer serverWorld = (WorldServer)EntityFox.this.worldObj;
	         EntityFox EntityFox = (EntityFox)this.theAnimal.createChild(this.targetMate);
	         if (EntityFox != null) {
	            EntityPlayer serverPlayerEntity = this.theAnimal.func_146083_cb();
	            EntityPlayer serverPlayerEntity2 = this.targetMate.func_146083_cb();
	            EntityPlayer serverPlayerEntity3 = serverPlayerEntity;
	            if (serverPlayerEntity != null) {
	               EntityFox.addTrustedUuid(serverPlayerEntity.getUniqueID());
	            } else {
	               serverPlayerEntity3 = serverPlayerEntity2;
	            }

	            if (serverPlayerEntity2 != null && serverPlayerEntity != serverPlayerEntity2) {
	               EntityFox.addTrustedUuid(serverPlayerEntity2.getUniqueID());
	            }
	            
	            if (serverPlayerEntity3 != null)
	            {
	            	serverPlayerEntity3.triggerAchievement(StatList.field_151186_x);
	            }

	            this.theAnimal.setGrowingAge(6000);
	            this.targetMate.setGrowingAge(6000);
	            this.theAnimal.resetInLove();
	            this.targetMate.resetInLove();
	            EntityFox.setGrowingAge(-24000);
	            EntityFox.setLocationAndAngles(this.theAnimal.posX, this.theAnimal.posY, this.theAnimal.posZ, 0.0F, 0.0F);
	            EntityFox.this.worldObj.spawnEntityInWorld(EntityFox);
	            EntityFox.this.worldObj.setEntityState(this.theAnimal, (byte)18);
	            if (EntityFox.this.worldObj.getGameRules().getGameRuleBooleanValue("doMobLoot"))
	            {
	                EntityFox.this.worldObj.spawnEntityInWorld(new EntityXPOrb(EntityFox.this.worldObj, this.theAnimal.posX, this.theAnimal.posY, this.theAnimal.posZ, EntityFox.this.rand.nextInt(7) + 1));
	            }

	         }
	      }
	   }

	   class AIAttack extends EntityAIAttackOnCollide {
	      public AIAttack(double speed, boolean pauseWhenIdle) {
	         super(EntityFox.this, speed, pauseWhenIdle);
	      }
	      
	      @Override
	      public void startExecuting() {
	         EntityFox.this.setRollingHead(false);
	         super.startExecuting();
	      }

	      @Override
	      public boolean shouldExecute() {
	         return !EntityFox.this.isSitting() && !EntityFox.this.isPlayerSleeping() && !EntityFox.this.isInSneakingPose() && !EntityFox.this.isWalking() && super.shouldExecute();
	      }
	   }

	   class AIMoveToHunt extends EntityAIBase {
	      public AIMoveToHunt() {
	         this.setMutexBits(AIMutex.MOVE | AIMutex.LOOK);
	      }

	      @Override
	      public boolean shouldExecute() {
	         if (EntityFox.this.isPlayerSleeping()) {
	            return false;
	         } else {
	            EntityLivingBase livingEntity = EntityFox.this.getAttackTarget();
	            return livingEntity != null && livingEntity.isEntityAlive() && EntityFox.CHICKEN_AND_RABBIT_FILTER.test(livingEntity) && EntityFox.this.getDistanceSqToEntity(livingEntity) > 36.0D && !EntityFox.this.isInSneakingPose() && !EntityFox.this.isRollingHead() && !EntityFox.this.isJumping;
	         }
	      }

	      @Override
	      public void startExecuting() {
	         EntityFox.this.setSitting(false);
	         EntityFox.this.setWalking(false);
	      }

	      @Override
	      public void resetTask() {
	         EntityLivingBase livingEntity = EntityFox.this.getAttackTarget();
	         if (livingEntity != null && EntityFox.canJumpChase(EntityFox.this, livingEntity)) {
	            EntityFox.this.setRollingHead(true);
	            EntityFox.this.setCrouching(true);
	            EntityFox.this.getNavigator().clearPathEntity();
	            EntityFox.this.getLookHelper().setLookPositionWithEntity(livingEntity, (float)EntityFox.this.getBodyYawSpeed(), (float)EntityFox.this.getLookPitchSpeed());
	         } else {
	            EntityFox.this.setRollingHead(false);
	            EntityFox.this.setCrouching(false);
	         }

	      }

	      @Override
	      public void updateTask() {
	         EntityLivingBase livingEntity = EntityFox.this.getAttackTarget();
	         EntityFox.this.getLookHelper().setLookPositionWithEntity(livingEntity, (float)EntityFox.this.getBodyYawSpeed(), (float)EntityFox.this.getLookPitchSpeed());
	         if (EntityFox.this.getDistanceSqToEntity(livingEntity) <= 36.0D) {
	            EntityFox.this.setRollingHead(true);
	            EntityFox.this.setCrouching(true);
	            EntityFox.this.getNavigator().clearPathEntity();
	         } else {
	            EntityFox.this.getNavigator().tryMoveToEntityLiving(livingEntity, 1.5D);
	         }

	      }
	   }

	   class FoxMoveHelper extends EntityMoveHelper {
	      public FoxMoveHelper() {
	         super(EntityFox.this);
	      }

	      @Override
	      public void onUpdateMoveHelper() {
	         if (EntityFox.this.wantsToPickupItem()) {
	            super.onUpdateMoveHelper();
	         }

	      }
	   }
/*
	   class PickupItemGoal extends EntityAIBase {
	      public PickupItemGoal() {
	         this.setControls(EnumSet.of(Goal.Control.MOVE));
	      }

	      public boolean canStart() {
	         if (!EntityFox.this.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty()) {
	            return false;
	         } else if (EntityFox.this.getAttackTarget() == null && EntityFox.this.getAttacker() == null) {
	            if (!EntityFox.this.wantsToPickupItem()) {
	               return false;
	            } else if (EntityFox.this.rand.nextInt(10) != 0) {
	               return false;
	            } else {
	               List list = EntityFox.this.worldObj.getEntitiesByClass(EntityItem.class, EntityFox.this.boundingBox.expand(8.0D, 8.0D, 8.0D), EntityFox.PICKABLE_DROP_FILTER);
	               return !list.isEmpty() && EntityFox.this.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty();
	            }
	         } else {
	            return false;
	         }
	      }

	      public void tick() {
	         List list = EntityFox.this.worldObj.getEntitiesByClass(EntityItem.class, EntityFox.this.boundingBox.expand(8.0D, 8.0D, 8.0D), EntityFox.PICKABLE_DROP_FILTER);
	         ItemStack itemStack = EntityFox.this.getEquippedStack(EquipmentSlot.MAINHAND);
	         if (itemStack.isEmpty() && !list.isEmpty()) {
	            EntityFox.this.getNavigator().startMovingTo((Entity)list.get(0), 1.2000000476837158D);
	         }

	      }

	      public void start() {
	         List list = EntityFox.this.worldObj.getEntitiesByClass(EntityItem.class, EntityFox.this.boundingBox.expand(8.0D, 8.0D, 8.0D), EntityFox.PICKABLE_DROP_FILTER);
	         if (!list.isEmpty()) {
	            EntityFox.this.getNavigator().startMovingTo((Entity)list.get(0), 1.2000000476837158D);
	         }

	      }
	   }
	   
	class AvoidPlayerGoal extends EntityAIAvoidEntity {
		/**
		 * Returns whether the EntityAIBase should begin execution.
		 *//*
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
	}*/

	   public static enum Type {
	      RED(0, "red", new BiomeGenBase[]{BiomeGenBase.taiga, BiomeGenBase.taigaHills, DUtil.getMutation(BiomeGenBase.taiga), BiomeGenBase.megaTaiga, BiomeGenBase.megaTaigaHills}), // GIANT_SPRUCE_TAIGA?
	      SNOW(1, "snow", new BiomeGenBase[]{BiomeGenBase.coldTaiga, BiomeGenBase.coldTaigaHills, DUtil.getMutation(BiomeGenBase.coldTaiga)});

	      private static final EntityFox.Type[] TYPES = (EntityFox.Type[])Arrays.stream(values()).sorted(Comparator.comparingInt(EntityFox.Type::getId)).toArray((i) -> {
	         return new EntityFox.Type[i];
	      });
	      private static final Map NAME_TYPE_MAP = (Map)Arrays.stream(values()).collect(Collectors.toMap(EntityFox.Type::getKey, (type) -> {
	         return type;
	      }));
	      private final int id;
	      private final String key;
	      private final List biomes;

	      private Type(int id, String key, BiomeGenBase... registryKeys) {
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

	      public static EntityFox.Type byName(String name) {
	         return (EntityFox.Type)NAME_TYPE_MAP.getOrDefault(name, RED);
	      }

	      public static EntityFox.Type fromId(int id) {
	         if (id < 0 || id > TYPES.length) {
	            id = 0;
	         }

	         return TYPES[id];
	      }

	      public static EntityFox.Type fromBiome(Optional optional) {
	         return optional.isPresent() && SNOW.biomes.contains(optional.get()) ? SNOW : RED;
	      }
	   }
}

