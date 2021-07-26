package makamys.dmod.entity;

import static makamys.dmod.entity.EntityFox.Ability.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.UUID;

import makamys.dmod.ConfigDMod;
import makamys.dmod.DMod;
import makamys.dmod.ai.EntityAIFollowOwnerEx;
import makamys.dmod.ai.EntityAIPanicWithTimeout;
import makamys.dmod.compat.Compat;
import makamys.dmod.compat.Compat.BerryBushState;
import makamys.dmod.constants.AIMutex;
import makamys.dmod.constants.NBTType;
import makamys.dmod.etfuturum.BlockPos;
import makamys.dmod.future.entity.EntityFuture;
import makamys.dmod.future.entity.ai.EntityAIDiveJump;
import makamys.dmod.future.entity.ai.EntityAIFleeSunModern;
import makamys.dmod.future.entity.ai.EntityAIModernAvoidEntity;
import makamys.dmod.future.entity.ai.EntityAIMoveToTargetPos;
import makamys.dmod.future.entity.ai.EntityAINearestAttackableTargetEx;
import makamys.dmod.future.entity.ai.ModernEntityLookHelper;
import makamys.dmod.future.entity.ai.TargetPredicate;
import makamys.dmod.future.entity.item.EntityItemFuture;
import makamys.dmod.future.entity.passive.EntityAnimalFuture;
import makamys.dmod.future.entity.passive.PassiveEntityEmulator;
import makamys.dmod.future.item.ItemStackFuture;
import makamys.dmod.future.predicate.entity.EntityPredicates;
import makamys.dmod.future.util.MathHelperFuture;
import makamys.dmod.future.world.EntityViewEmulator;
import makamys.dmod.util.DUtil;
import makamys.dmod.util.WeightedRandomItem;
import net.minecraft.block.Block;
import net.minecraft.command.IEntitySelector;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;

public class EntityFox extends EntityAnimalFuture implements ITameable {
	private static final int OWNER = 18;
	private static final int OTHER_TRUSTED = 19;
	private static final int TYPE = 20;
	private static final int FOX_FLAGS = 21;
	private static final int EXPERIENCE = 22;
	private static final IEntitySelector PICKABLE_DROP_FILTER;
	private static final IEntitySelector FOOD_DROP_FILTER;
	private static final IEntitySelector TOOL_DROP_FILTER;
	private static final IEntitySelector SWORD_DROP_FILTER;
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
	private boolean isFleeingNearDeath;
	private EntityLivingBase friend;
	private boolean searchingForWeapon;
	private boolean followOwner;
	public int finishedSwings;
	private float lastActualSwingProgress;
	
	public static boolean trustEveryone = Boolean.parseBoolean(System.getProperty("dmod.foxTrustEveryone", "false"));

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
		this.dataWatcher.addObject(EXPERIENCE, Float.valueOf(0));
	}

	protected void initTasks() {
		 this.followChickenAndRabbitTask = new EntityAINearestAttackableTargetEx(this, EntityLiving.class, 10, true, false, DUtil.entitySelector(livingEntity -> {
		 	return livingEntity instanceof EntityChicken || ConfigDMod.rabbitEntities.contains(livingEntity.getClass());
		 }));
		/*this.followBabyTurtleGoal = new FollowTargetGoal(this, TurtleEntity.class, 10, false, false, TurtleEntity.BABY_TURTLE_ON_LAND_FILTER);
		this.followFishGoal = new FollowTargetGoal(this, FishEntity.class, 20, false, false, (livingEntity) -> {
			return livingEntity instanceof SchoolingFishEntity;
		});*/
		this.tasks.addTask(0, new EntityFox.AISwim());
		this.tasks.addTask(1, new EntityFox.AIStopWandering());
		this.tasks.addTask(1, new EntityFox.AIPickupItemWhenWounded());
		this.tasks.addTask(2, new EntityFox.AIEscapeWhenNotAggressive(2.2D));
		this.tasks.addTask(2, new EntityFox.AIEscapeWhenAggressiveAndWounded(2.2D));
		this.tasks.addTask(3, new EntityFox.AIMate(1.0D));
		this.tasks.addTask(4, new EntityAIModernAvoidEntity(this, EntityPlayer.class, 16.0F, 1.6D, 1.4D, (livingEntity) -> {
			return NOTICEABLE_PLAYER_FILTER.test(livingEntity) && !this.canTrust(livingEntity.getUniqueID()) && !this.isAggressive();
		}));
		this.tasks.addTask(4, new EntityAIModernAvoidEntity(this, EntityWolf.class, 8.0F, 1.6D, 1.4D, (livingEntity) -> {
			return !((EntityWolf)livingEntity).isTamed() && !this.isAggressive();
		}));
		this.tasks.addTask(4, new EntityAIModernAvoidEntity(this, EntityCreeper.class, 4F, 1.0D, 1.6D, (livingEntity) -> {
			return ((EntityCreeper)livingEntity).getCreeperState() == 1;
		}, true) {
			@Override
			public boolean shouldExecute() {
				return EntityFox.this.hasAbility(Ability.AVOID_PRIMED_CREEPERS) && super.shouldExecute();
			}
		});
		/*this.tasks.addTask(4, new EntityAIModernAvoidEntity(this, PolarBearEntity.class, 8.0F, 1.6D, 1.4D, (livingEntity) -> {
			return !this.isAggressive();
		}));*/
		this.tasks.addTask(5, new EntityFox.AIMoveToHunt());
		this.tasks.addTask(5, new EntityFox.AIPickupWeapon());
		this.tasks.addTask(6, new EntityFox.AIJumpChase());
		this.tasks.addTask(6, new EntityFox.AIAvoidDaylight(1.25D));
		// set this to false to make foxes more aggressive
		// TODO make them more aggressive when they or their owner is low on health
		this.tasks.addTask(7, new EntityFox.AIAttack(1.2000000476837158D, true));
		this.tasks.addTask(7, new EntityFox.AIDelayedCalmDown());
		this.tasks.addTask(8, new EntityFox.AIFollowParent(this, 1.25D));
		this.tasks.addTask(8, new EntityAIFollowOwnerEx(this, 1.3D, 24.0F, 8.0F) {
			@Override
			public boolean shouldExecute() {
				return EntityFox.this.hasAbility(FOLLOW_OWNER) && super.shouldExecute();
			}
		});
	 	// TODO (hint: this.worldObj.villageCollectionObj.findNearestVillage)
		//this.tasks.addTask(9, new EntityFox.GoToVillageGoal(32, 200));
		this.tasks.addTask(10, new EntityFox.AIEatSweetBerries(1.2000000476837158D, 12, 2));
		this.tasks.addTask(10, new EntityAILeapAtTarget(this, 0.4F));
		this.tasks.addTask(11, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(11, new EntityFox.AIPickupItem());
		this.tasks.addTask(12, new EntityFox.AILookAtEntity(this, EntityPlayer.class, 24.0F));
		this.tasks.addTask(13, new EntityFox.AISitDownAndLookAround());
		this.targetTasks.addTask(3, new EntityFox.AIDefendFriend(EntityLiving.class, false, false, (livingEntity) -> {
			return livingEntity instanceof EntityLivingBase
					&& JUST_ATTACKED_SOMETHING_FILTER.test((EntityLivingBase) livingEntity)
					&& !this.canTrust(livingEntity.getUniqueID());
		}));
	}

	@Override
	public String getEatSound(ItemStack stack) {
		return DMod.MODID + ":entity.fox.eat";
	}
	
	private void trustAllPlayers() {
		for(Object obj : ((WorldServer)worldObj).playerEntities) {
			UUID uuid = ((Entity)obj).getUniqueID();
			this.dataWatcher.updateObject(OWNER, uuid.toString());
		}
	}
	
	public void onLivingUpdate() {
		if(this.worldObj.isRemote) {
			this.updateArmSwingProgress();
			float actualSwingProgress = this.getSwingProgress(1);
			if(actualSwingProgress < lastActualSwingProgress) {
				finishedSwings++;
			}
			lastActualSwingProgress = actualSwingProgress;
		}
		
		if (!this.worldObj.isRemote && this.isEntityAlive()/* && this.canMoveVoluntarily()*/) {
	 	 if(trustEveryone && this.ticksExisted % 20 == 0) {
	 		 trustAllPlayers();
	 	 }
	 	  
	 	  ++this.eatingTime;
			ItemStack itemStack = this.getHeldItem();
			if (this.canEat(itemStack)) {
				int eatDelay = this.hasAbility(FASTER_EATING)
						? (int)(MathHelper.clamp_float(this.getHealth() < this.getMaxHealth() ? (this.getHealth() - 2f) / this.getMaxHealth() / 4f : 1f, 0f, 1f) * 560f)
						: 560;
				if (this.eatingTime > eatDelay + 40) {
					ItemStack itemStack2 = ItemStackFuture.finishUsing(itemStack, this.worldObj, this);
					if (!ItemStackFuture.isEmpty(itemStack2)) {
						this.setCurrentItemOrArmor(0, itemStack2);
					} else {
						this.setCurrentItemOrArmor(0, null);
					}

					this.eatingTime = 0;
				} else if (this.eatingTime > eatDelay && this.rand.nextFloat() < 0.1F) {
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
		
		if(this.getPetOwner() != null && this.getDistanceSqToEntity(this.getPetOwner()) > 64f * 64f) {
			followOwner = false;
		}

		if (this.isPlayerSleeping() || this.isImmobile()) {
			this.isJumping = false;
			this.moveStrafing = 0.0F;
			this.moveForward = 0.0F;
		}

		super.onLivingUpdate();
		if (this.isAggressive() && this.rand.nextFloat() < 0.05F) {
			this.playSound(DMod.MODID + ":entity.fox.aggro", 1.0F, 1.0F);
		}

	}
	
	@Override
	public void setCurrentItemOrArmor(int p_70062_1_, ItemStack p_70062_2_) {
		if(p_70062_1_ == 0) {
			 finishedSwings = 0;
		}
		super.setCurrentItemOrArmor(p_70062_1_, p_70062_2_);
	}
	
	// XXX not called
	protected boolean isImmobile() {
		return this.isDead;
	}

	private boolean canEat(ItemStack stack) {
		return stack != null && stack.getItem() instanceof ItemFood && (this.getAttackTarget() == null || EntityFox.this.hasAbility(Ability.WOUNDED_AI) && EntityFox.this.getHealth() < EntityFox.this.getMaxHealth() / 2f) && this.onGround && !this.isPlayerSleeping();
	}

	protected void initEquipment() {
		if (this.rand.nextFloat() < 0.2F) {
	 	  List<WeightedRandomItem<Item>> items = ConfigDMod.foxMouthItems;
	 	  if(!items.isEmpty()) {
		 	  WeightedRandomItem<Item> choice = (WeightedRandomItem<Item>)WeightedRandom.getRandomItem(getRNG(), items);
				this.setCurrentItemOrArmor(0, new ItemStack(choice.data));
	 	  }
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
		EntityFox.Type type = EntityFox.Type.fromBiome(worldObj.getBiomeGenForCoords((int)this.posX, (int)this.posZ));
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
	
	public void healByFood(ItemStack stack) {
		Item item = stack.getItem();
		if(item instanceof ItemFood){
			ItemFood food = (ItemFood)item;
			int healAmount = food.func_150905_g(null);
			this.heal(healAmount);
		}
	}
	
	@Override
	public ItemStack eatFood(World world, ItemStack stack) {
		if(this.hasAbility(EAT_TO_HEAL)) {
			healByFood(stack);
		}
		return super.eatFood(world, stack);
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
	
	private void setExperience(float exp) {
		this.dataWatcher.updateObject(EXPERIENCE, exp);
	}
	
	private void addExperience(float exp) {
		setExperience(getExperience() + exp);
	}
	
	public float getExperience() {
		return this.dataWatcher.getWatchableObjectFloat(EXPERIENCE);
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
		if(this.getExperience() != 0) {
			tag.setFloat("Experience", this.getExperience());
		}
		if(this.followOwner) {
			tag.setBoolean("FollowOwner", this.followOwner);
		}
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
			this.setSitting(false);//tag.getBoolean("Sitting")); // disabled to avoid bugs
			this.setCrouching(tag.getBoolean("Crouching"));
			if (this.worldObj instanceof WorldServer) {
				this.addTypeSpecificTasks();
			}
			if(tag.hasKey("Experience")) {
				this.setExperience(tag.getFloat("Experience"));
			}
			if(tag.hasKey("FollowOwner")) {
				this.followOwner = tag.getBoolean("FollowOwner");
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
		if(!this.hasAbility(BETTER_PICKUP)) {
			return canPickupItemOld(stack);
		}
		
		Item item = stack.getItem();
		ItemStack itemStack = this.getEquipmentInSlot(0);
		if(itemStack == null) {
			return true;
		} else if(this.eatingTime > 0){
			Item current = itemStack.getItem();
			boolean holdingWeapon = current instanceof ItemSword;
			boolean fullyHealed = this.getHealth() == this.getMaxHealth();
			if(item instanceof ItemFood) {
				return !(current instanceof ItemFood) && (!holdingWeapon || (!fullyHealed && this.getAITarget() == null && this.getAttackTarget() == null));
			} else if(item instanceof ItemSword) {
				return !(current instanceof ItemFood) || !((!fullyHealed && this.getAITarget() == null && this.getAttackTarget() == null));
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	private boolean canPickupItemOld(ItemStack stack) {
		Item item = stack.getItem();
		ItemStack itemStack = this.getEquipmentInSlot(0);
		return itemStack == null || this.eatingTime > 0 && item instanceof ItemFood && !(itemStack.getItem() instanceof ItemFood);
	}

	private boolean canEatOld(ItemStack stack) {
		return stack != null && stack.getItem() instanceof ItemFood && this.getAttackTarget() == null && this.onGround && !this.isPlayerSleeping();
	}

	private void spit(ItemStack stack) {
		if (stack != null && !this.worldObj.isRemote) {
	 	  Vec3 rv = EntityFuture.getRotationVector(this);
			EntityItem EntityItem = new EntityItem(this.worldObj, this.posX + rv.xCoord, this.posY + 1.0D, this.posZ + rv.zCoord, stack);
			EntityItem.delayBeforeCanPickup = 40;
			EntityItem.func_145799_b(getUniqueID().toString()); // is this OK?
			this.playSound(DMod.MODID + ":entity.fox.spit", 1.0F, 1.0F);
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
			EntityPlayer dropper = DMod.proxy.itemDropperMap.getIfPresent(item);
			
			if(isBreedingItem(itemStack) && dropper != null && dropper.getUniqueID().equals(getTrustedUuids().get(0))) {
				followOwner = true;
			}
			
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
		return stack != null && ConfigDMod.foxBreedingItems.contains(stack.getItem());
	}
	
	// Not a thing in 1.7.10 as eggs only spawn adult entities
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
		if(!p_70652_1_.isEntityAlive() && p_70652_1_ instanceof EntityMob) {
			EntityMob victim = (EntityMob)p_70652_1_;
			int exp = ReflectionHelper.getPrivateValue(EntityLiving.class, victim, "experienceValue", "field_70728_aV");
			float expModifier = this.worldObj.isDaytime() ? 0.5f : 1f;
			expModifier *= 1f + this.getRNG().nextFloat() * 0.2f;
			this.addExperience(exp * expModifier);
			DMod.LOGGER.debug("Earned " + exp * expModifier + " exp (now at " + this.getExperience() + ")");
		}
		if(result) {
			EntityFox.this.playSound(DMod.MODID + ":entity.fox.bite", 1.0F, 1.0F);
		}
		return result;
	}
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float damage) {
		if(Compat.isBerryBushDamageSource(source)) {
			return false;
		} else {
			if(source.getEntity() instanceof EntityMob) {
				int swordBlockAbility = this.getTieredAbilityLevel(SWORD_BLOCK, SWORD_BLOCK_II);
				if(this.hasAbility(FLEE_DODGE) && isFleeingNearDeath && getRNG().nextBoolean()) {
					return false;
				} else if(swordBlockAbility > 0 && getHeldItem() != null && getHeldItem().getItem() instanceof ItemSword && getRNG().nextInt(4 - swordBlockAbility) == 0) {
					this.playSound("random.anvil_land", 0.5F, 1.3f);
					damage /= 2f;
				} else if(this.hasAbility(WOUNDED_SCREECH) && this.getHealth() - damage <= EntityFox.this.getMaxHealth() / 4f && this.getHealth() > EntityFox.this.getMaxHealth() / 4f) {
					this.playSound(DMod.MODID + ":entity.fox.screech", 2F, getSoundPitch() * 1.4f);
				}
			}
			return super.attackEntityFrom(source, damage);
		}
	}
	
	@Override
	public EntityLivingBase getPetOwner() {
		UUID uUID = EntityFox.this.getTrustedUuids().get(0);

		if (uUID != null && EntityFox.this.worldObj instanceof WorldServer) {
			// Assuming owner is a player
			Entity entity = ((WorldServer)EntityFox.this.worldObj).func_152378_a(uUID); // getPlayerByUuid
			if (entity instanceof EntityLivingBase) {
				EntityLivingBase livingEntity = (EntityLivingBase)entity;
				return livingEntity;
			}
		}
		return null;
	}
	
	@Override
	public boolean isPetSitting() {
		return !followOwner;
	}
	
	public int getLootingLevel() {
		int lootingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.looting.effectId, this.getHeldItem());
		return Math.max(this.hasAbility(INTRINSIC_LOOTING) ? 1 : 0, lootingLevel);
	}
	
	public boolean hasAbility(Ability ability) {
		switch(ConfigDMod.foxAbilityMode) {
		case NORMAL:
			return this.getExperience() >= ability.minExp;
		case UNLOCK_ALL:
			return true;
		case UNLOCK_NONE:
			return false;
		default:
			throw new IllegalArgumentException();
		}
		
	}
	
	public int getTieredAbilityLevel(Ability...abilities) {
		for(int i = 0; i < abilities.length; i++)
			if(this.getExperience() >= abilities[i].minExp) {
				return i + 1;
		}
		return 0;
		
	}
	
	// TODO
	/*
		@SideOnly(Side.CLIENT)
		public Vec3d method_29919() {
			return new Vec3d(0.0D, (double)(0.55F * this.getStandingEyeHeight()), (double)(this.getWidth() * 0.4F));
		}
	 */

	static {
		PICKABLE_DROP_FILTER = DUtil.entitySelector(entityItem -> {
			return !EntityItemFuture.cannotPickUp((EntityItem)entityItem) && entityItem.isEntityAlive();
		});
		FOOD_DROP_FILTER = DUtil.entitySelector(entityItem -> {
			return ((EntityItem)entityItem).getEntityItem().getItem() instanceof ItemFood && entityItem.isEntityAlive();
		});
		TOOL_DROP_FILTER = DUtil.entitySelector(entityItem -> {
			return ((EntityItem)entityItem).getEntityItem().getItem() instanceof ItemTool && entityItem.isEntityAlive();
		});
		SWORD_DROP_FILTER = DUtil.entitySelector(entityItem -> {
			return ((EntityItem)entityItem).getEntityItem().getItem() instanceof ItemSword && entityItem.isEntityAlive();
		});
		JUST_ATTACKED_SOMETHING_FILTER = (entity) -> {
			if (!(entity instanceof EntityLiving)) {
				return false;
			} else {
		  	 return true;
				//EntityLiving livingEntity = (EntityLiving)entity;
				//return livingEntity.getLastAttacker() != null && livingEntity.getLastAttackerTime() < livingEntity.ticksExisted + 600;
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

	class AIEscapeWhenNotAggressive extends EntityAIPanicWithTimeout {
		public AIEscapeWhenNotAggressive(double speed) {
			super(EntityFox.this, speed);
		}
		
		@Override
		public boolean shouldExecute() {
			return !EntityFox.this.isAggressive() && super.shouldExecute();
		}
	}
	
	class AIEscapeWhenAggressiveAndWounded extends EntityAIPanicWithTimeout {
		public AIEscapeWhenAggressiveAndWounded(double speed) {
			super(EntityFox.this, speed);
		}
		
		@Override
		public void startExecuting() {
			EntityFox.this.isFleeingNearDeath = true;
			super.startExecuting();
		}
		
		@Override
		public boolean shouldExecute() {
			if(!EntityFox.this.hasAbility(WOUNDED_AI)) {
				return false;
			}
			return EntityFox.this.isAggressive() && (EntityFox.this.getHealth() <= EntityFox.this.getMaxHealth() / 4f) && super.shouldExecute();
		}
		
		@Override
		public void resetTask() {
			EntityFox.this.isFleeingNearDeath = false;
			super.resetTask();
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

	public class AIEatSweetBerries extends EntityAIMoveToTargetPos {
		protected int timer;

		public AIEatSweetBerries(double speed, int range, int maxYDifference) {
			super(EntityFox.this, speed, range, maxYDifference);
		}

		@Override
		public double getDesiredSquaredDistanceToTarget() {
			return 2.0D;
		}

		@Override
		public boolean shouldResetPath() {
			return this.tryingTime % 100 == 0;
		}

		@Override
		protected boolean isTargetPos(World world, int bx, int by, int bz) {
	 	  BerryBushState bbs = Compat.getBerryBushState(world, bx, by, bz);
	 	  return bbs != null ? bbs.getAge() >= 2 : false;
		}

		@Override
		public void updateTask() {
			if (this.hasReached()) {
				if (this.timer >= 40) {
					this.eatSweetBerry();
				} else {
					++this.timer;
				}
			} else if (!this.hasReached() && EntityFox.this.rand.nextFloat() < 0.05F) {
				EntityFox.this.playSound(DMod.MODID + ":entity.fox.sniff", 1.0F, 1.0F);
			}

			super.updateTask();
		}

		protected void eatSweetBerry() {
			if (EntityFox.this.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing")) {
		  	 int x = this.targetPos.getX();
		  	 int y = this.targetPos.getY();
		  	 int z = this.targetPos.getZ();
		  	 Block block = EntityFox.this.worldObj.getBlock(x, y, z);
		  	 BerryBushState bbs = Compat.getBerryBushState(EntityFox.this.worldObj, x, y, z);
		  			 
				if (bbs != null) {
					int i = bbs.getAge();
					//blockState.with(SweetBerryBushBlock.AGE, 1); // NOP?
					int j = 1 + EntityFox.this.worldObj.rand.nextInt(2) + (i == 3 ? 1 : 0);
					ItemStack itemStack = EntityFox.this.getHeldItem();
					if (itemStack == null) {
						EntityFox.this.setCurrentItemOrArmor(0, new ItemStack(bbs.handler.getSweetBerryItem()));
						--j;
					}

					if (j > 0) {
						block.dropBlockAsItem(EntityFox.this.worldObj, x, y, z, new ItemStack(bbs.handler.getSweetBerryItem(), j));
					}

					EntityFox.this.playSound("item.sweet_berries.pick_from_bush", 1.0F, 1.0F);
					EntityFox.this.worldObj.setBlockMetadataWithNotify(x, y, z, bbs.getMetaForNewAge(1), 2);
				}
			}
		}

		@Override
		public boolean shouldExecute() {
			return !EntityFox.this.isPlayerSleeping() && super.shouldExecute();
		}

		@Override
		public void startExecuting() {
			this.timer = 0;
			EntityFox.this.setSitting(false);
			super.startExecuting();
		}
	}

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
			return isIdle() && EntityFox.this.rand.nextFloat() < 0.02F;
		}
		
		private boolean isIdle() {
			return EntityFox.this.getEntityToAttack() == null && !EntityFox.this.isPlayerSleeping() && EntityFox.this.getAttackTarget() == null && EntityFox.this.getNavigator().noPath() && !this.canNotCalmDown() && !EntityFox.this.isChasing() && !EntityFox.this.isInSneakingPose() && (EntityFox.this.getHealth() > EntityFox.this.getMaxHealth() / 4f);
		}

		@Override
		public boolean continueExecuting() {
			return this.counter > 0 && isIdle();
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

	class AIDefendFriend extends EntityAINearestAttackableTargetEx {
		private EntityLivingBase offender;
		private int lastAttackedTime;
		TargetPredicate targetPredicate;

		public AIDefendFriend(Class targetEntityClass, boolean checkVisibility, boolean checkCanNavigate, Predicate<Entity> targetPredicate) {
			super(EntityFox.this, targetEntityClass, 10, checkVisibility, checkCanNavigate, DUtil.entitySelector(e -> targetPredicate.test(e)));
			this.targetPredicate = (new TargetPredicate()).setBaseMaxDistance(this.getTargetDistance()).setPredicate(targetPredicate);
			if(!checkVisibility) {
				// rationale for this: tall grass blocks the view of foxes. since they are so short, this greatly limits their helpfulness in
				// areas with tall grass
				this.targetPredicate.includeHidden();
			}
		}

		@Override
		public boolean shouldExecute() {
			if (this.targetChance > 0 && EntityFox.this.getRNG().nextInt(this.targetChance) != 0) {
				return false;
			} else {
				Iterator<UUID> var1 = EntityFox.this.getTrustedUuids().iterator();

				while(var1.hasNext()) {
					UUID uUID = (UUID)var1.next();
					if (uUID != null && EntityFox.this.worldObj instanceof WorldServer) {
						// Assuming owner is a player
						Entity entity = ((WorldServer)EntityFox.this.worldObj).func_152378_a(uUID); // getPlayerByUuid
						if (entity instanceof EntityLivingBase) {
					 	  EntityLivingBase livingEntity = (EntityLivingBase)entity;
							EntityFox.this.friend = livingEntity;
							this.offender = livingEntity.getAITarget();
							int i = livingEntity.func_142015_aE();
							return i != this.lastAttackedTime && this.canTrack(this.offender, this.targetPredicate);
						}
					}
				}

				return false;
			}
		}
		
		private boolean canTrack(EntityLivingBase target, TargetPredicate targetPredicate) {
	 	  if (target == null) {
	 				return false;
	 			} else if (!targetPredicate.test(EntityFox.this, target)) {
	 				return false;
	 			} else if (!EntityFox.this.isWithinHomeDistance(
	 		 		MathHelper.floor_double(target.posX),
						MathHelper.floor_double(target.posY),
						MathHelper.floor_double(target.posZ))) {
	 				return false;
	 			} else {
	 				if (this.nearbyOnly) {
	 					if (--this.targetSearchDelay <= 0) {
	 						this.targetSearchStatus = 0;
	 					}

	 					if (this.targetSearchStatus == 0) {
	 						this.targetSearchStatus = this.canEasilyReach(target) ? 1 : 2;
	 					}

	 					if (this.targetSearchStatus == 2) {
	 						return false;
	 					}
	 				}

	 				return true;
	 			}
		}

		@Override
		public void startExecuting() {
			// this.setTargetEntity(this.offender); // NOP
			this.targetEntity = this.offender;
			if (EntityFox.this.friend != null) {
				this.lastAttackedTime = EntityFox.this.friend.getLastAttackerTime();
			}
			
			EntityFox.this.playSound(DMod.MODID + ":entity.fox.aggro", 1.0F, 1.0F);
			EntityFox.this.setAggressive(true);
			EntityFox.this.stopSleeping();
			super.startExecuting();
		}
		
		@Override
		public void resetTask() {
			EntityFox.this.friend = null;
			super.resetTask();
		}
	}

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
				
				EntityFox.setExperience(Math.max(0, (((EntityFox)this.theAnimal).getExperience() + ((EntityFox)this.targetMate).getExperience()) / 2f - this.theAnimal.getRNG().nextFloat() * 10f));
				
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
		double baseSpeed;
		public AIAttack(double speed, boolean pauseWhenIdle) {
			super(EntityFox.this, speed, pauseWhenIdle);
			this.baseSpeed = speed;
		}
		
		@Override
		public void startExecuting() {
			EntityFox.this.setRollingHead(false);
			super.startExecuting();
		}
		
		@Override
		public void updateTask() {
			boolean strong = false;
			int agressionAbility = getTieredAbilityLevel(ESCALATE_AGRESSION, ESCALATE_AGRESSION_II);
			if(agressionAbility > 0) {
				float deadness = MathHelper.clamp_float(1f - (EntityFox.this.getHealth() / (EntityFox.this.getMaxHealth() / 2f)), 0f, 1f);
				int baseAgressionChance = 12 - (agressionAbility - 1) * 4;
				strong = EntityFox.this.getRNG().nextInt(baseAgressionChance - (int)((baseAgressionChance - 2) * deadness)) == 0;
				if((EntityFox.this.friend != null && EntityFox.this.friend.getHealth() < EntityFox.this.friend.getMaxHealth() / 4f) ){
					strong = true;
					EntityFox.this.getNavigator().setSpeed(baseSpeed * 1.5f);
				}
			}
			ReflectionHelper.setPrivateValue(EntityAIAttackOnCollide.class, this, !strong, "longMemory", "field_75437_f");
			super.updateTask();
		}

		@Override
		public boolean shouldExecute() {
			boolean result = !EntityFox.this.isSitting() && !EntityFox.this.isPlayerSleeping() && !EntityFox.this.isInSneakingPose() && !EntityFox.this.isWalking() && super.shouldExecute();
			return result;
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
	
	class AIPickupWeapon extends AIPickupItem {
		public AIPickupWeapon() {
			super();
		}
		
		private boolean isMajorFoe(EntityLivingBase e) {
			return e != null;
		}

		@Override
		public boolean shouldExecute() {
			if(!EntityFox.this.hasAbility(SEARCH_WEAPON_WHEN_AGGRESSIVE)) {
				return false;
			}
			if (EntityFox.this.getHealth() < EntityFox.this.getMaxHealth() / 2f && EntityFox.this.getHeldItem() != null && EntityFox.this.getHeldItem().getItem() instanceof ItemFood) {
				return false;
			} else if (isMajorFoe(EntityFox.this.getAttackTarget()) || isMajorFoe(EntityFox.this.getAITarget())) {
				if (!EntityFox.this.wantsToPickupItem()) {
					return false;
				} else if(EntityFox.this.getHeldItem() == null || !(EntityFox.this.getHeldItem().getItem() instanceof ItemSword || EntityFox.this.getHeldItem().getItem() instanceof ItemTool)) {
					return canPickUpNearbyItemStack();
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
		
		@Override
		public void startExecuting() {
			EntityFox.this.searchingForWeapon = true;
			super.startExecuting();
		}
		
		@Override
		public void resetTask() {
			EntityFox.this.searchingForWeapon = false;
			super.resetTask();
		}
		
		@Override
		public void updateTask() {
			moveToNearbyItemStack(false);
		}

		@Override
		protected double getFetchSpeed() {
			return 1.6D;
		}
		
		@Override
		protected List getNearbyItemStacks() {
			List swords = getNearbyItemStacksFiltered(EntityFox.SWORD_DROP_FILTER);
			if(!swords.isEmpty()) {
				return swords;
			} else {
				return getNearbyItemStacksFiltered(EntityFox.TOOL_DROP_FILTER);
			}
		}
	}
	
	class AIPickupItemWhenWounded extends AIPickupItem {
		public AIPickupItemWhenWounded() {
			super();
		}

		@Override
		public boolean shouldExecute() {
			if(!EntityFox.this.hasAbility(WOUNDED_AI)) {
				return false;
			}
			boolean hungry = EntityFox.this.getHealth() < EntityFox.this.getMaxHealth() / 2f
					&& (EntityFox.this.getHeldItem() == null || !(EntityFox.this.getHeldItem().getItem() instanceof ItemFood));
			if(!hungry) {
				return false;
			} else {
				if (!EntityFox.this.wantsToPickupItem()) {
					return false;
				} else {
					return canPickUpNearbyItemStack();
				}
			}
		}
		
		@Override
		public void updateTask() {
			moveToNearbyItemStack(false);
		}

		@Override
		protected double getFetchSpeed() {
			return MathHelperFuture.lerp(1f - (EntityFox.this.getHealth() / (EntityFox.this.getMaxHealth() / 2f)), 1.2000000476837158D, 2.2D);
		}
		
		@Override
		protected List getNearbyItemStacks() {
			return EntityFox.this.worldObj.selectEntitiesWithinAABB(EntityItem.class, EntityFox.this.boundingBox.expand(8.0D, 8.0D, 8.0D), EntityFox.FOOD_DROP_FILTER);
		}
		
		@Override
		protected boolean prefersItem(ItemStack is) {
			ItemStack current = EntityFox.this.getHeldItem();
			return current == null;
		}
	}

	class AIPickupItem extends EntityAIBase {
		public AIPickupItem() {
			this.setMutexBits(AIMutex.MOVE);
		}

		@Override
		public boolean shouldExecute() { 
			if (EntityFox.this.getHeldItem() != null) {
				return false;
			} else if ((EntityFox.this.getAttackTarget() == null && EntityFox.this.getAITarget() == null)) {
				if (!EntityFox.this.wantsToPickupItem()) {
					return false;
				} else if (EntityFox.this.rand.nextInt(10) != 0) {
					return false;
				} else {
					return canPickUpNearbyItemStack();
				}
			} else {
				return false;
			}
		}

		@Override
		public void updateTask() {
			moveToNearbyItemStack(true);
		}

		@Override
		public void startExecuting() {
			moveToNearbyItemStack(false);
		}
		
		protected double getFetchSpeed() {
			return 1.2000000476837158D;
		}
		
		protected List<EntityItem> getNearbyItemStacksFiltered(IEntitySelector selector) {
			return EntityFox.this.worldObj.selectEntitiesWithinAABB(EntityItem.class, EntityFox.this.boundingBox.expand(8.0D, 8.0D, 8.0D), selector);
		}
		
		protected List<EntityItem> getNearbyItemStacks() {
			return getNearbyItemStacksFiltered(EntityFox.PICKABLE_DROP_FILTER);
		}
		
		protected void moveToNearbyItemStack(boolean onlyIfNotHoldingItem) {
			List<EntityItem> list = getNearbyItemStacks();
			ItemStack itemStack = EntityFox.this.getHeldItem();
			EntityItem ei = (EntityItem)list.get(0);
			if ((!onlyIfNotHoldingItem || prefersItem(ei.getEntityItem())) && !list.isEmpty()) {
				EntityFox.this.getNavigator().tryMoveToEntityLiving(ei, getFetchSpeed());
			}
		}
		
		protected boolean prefersItem(ItemStack is) {
			ItemStack current = EntityFox.this.getHeldItem();
			return current == null;
		}
		
		protected boolean canPickUpNearbyItemStack() {
			List<EntityItem> nearbyItemStacks = getNearbyItemStacks();
			return !nearbyItemStacks.isEmpty() && EntityFox.this.canPickupItem(nearbyItemStacks.get(0).getEntityItem());
		}
	}

	public static enum Type {
		RED(0, "red"),
		SNOW(1, "snow");

		private static final EntityFox.Type[] TYPES = (EntityFox.Type[])Arrays.stream(values()).sorted(Comparator.comparingInt(EntityFox.Type::getId)).toArray((i) -> {
			return new EntityFox.Type[i];
		});
		private static final Map NAME_TYPE_MAP = (Map)Arrays.stream(values()).collect(Collectors.toMap(EntityFox.Type::getKey, (type) -> {
			return type;
		}));
		private final int id;
		private final String key;

		private Type(int id, String key) {
			this.id = id;
			this.key = key;
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

		public static EntityFox.Type fromBiome(BiomeGenBase bgb) {
			return bgb != null && BiomeDictionary.isBiomeOfType(bgb, BiomeDictionary.Type.SNOWY) ? SNOW : RED;
		}
	}
	
	public static enum AbilityMode { NORMAL, UNLOCK_ALL, UNLOCK_NONE }
	
	public static enum Ability {
		WOUNDED_SCREECH(5),
		
		EAT_TO_HEAL(25),
		BETTER_PICKUP(25),
		FASTER_EATING(25),
		WOUNDED_AI(25),
		AVOID_PRIMED_CREEPERS(25),
		FOLLOW_OWNER(25),
		
		IMPROVED_HELD_ITEM_RENDERING(50),
		FLEE_DODGE(50),
		SEARCH_WEAPON_WHEN_AGGRESSIVE(50),
		ESCALATE_AGRESSION(50),
		INTRINSIC_LOOTING(50),
		
		SWORD_BLOCK(70),
		
		ESCALATE_AGRESSION_II(100),
		
		SWORD_BLOCK_II(150),
		SWORD_SWING_ANIMATION(150),
		;
		
		private int minExp;
		
		private Ability(int minExp) {
			this.minExp = minExp;
		}
	}
}

