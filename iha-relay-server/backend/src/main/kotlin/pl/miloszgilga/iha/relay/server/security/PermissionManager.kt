package pl.miloszgilga.iha.relay.server.security

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.enums.enumEntries

private val log = KotlinLogging.logger {}

internal interface MaskBearer {
  val permissionsMask: Long
}

internal class PermissionManager<T>(permissions: Iterable<T>) where T : Enum<T>, T : BitmaskPermission {
  private val permissionMap = permissions.associateBy { it.name.uppercase() }
  private val adminMask: Long = permissions
    .filter { it.admin }
    .fold(0L) { acc, perm -> acc or perm.bit }

  val permissions: Collection<T>
    get() = permissionMap.values

  fun isUserAdmin(mask: Long) = (mask and adminMask) != 0L

  fun generateMask(permissionNames: Collection<String>?): Long = when {
    permissionNames.isNullOrEmpty() -> 0L
    else -> permissionNames
      .asSequence()
      .filter { it.isNotBlank() }
      .mapNotNull { name ->
        permissionMap[name.uppercase()].also { perm ->
          if (perm == null) log.warn { "Skipping unknown permission name '$name' during mask generation" }
        }
      }
      .fold(0L) { acc, perm -> acc or perm.bit }
      .also { mask -> log.debug { "Generated mask: $mask for requested permissions: $permissionNames" } }
  }

  fun hasPermission(
    userMask: Long,
    requiredPermission: BitmaskPermission,
    overrideForAdmin: Boolean = true
  ): Boolean = when {
    overrideForAdmin && isUserAdmin(userMask) -> true.also {
      log.debug { "Access granted: user has ADMIN override" }
    }

    else -> ((userMask and requiredPermission.bit) != 0L).also { hasPerm ->
      log.debug { "Permission '$requiredPermission' evaluation result: $hasPerm" }
    }
  }

  fun hasPermission(
    userMask: Long,
    requiredRoleOrPermission: String,
    overrideForAdmin: Boolean = true,
  ): Boolean = when (val permission = permissionMap[requiredRoleOrPermission.uppercase()]) {
    null -> false.also {
      log.warn { "Unable to find followed permission: $requiredRoleOrPermission" }
    }

    else -> hasPermission(userMask, permission, overrideForAdmin)
  }

  fun hasAnyPermission(userMask: Long, vararg requiredPermissions: BitmaskPermission): Boolean = when {
    requiredPermissions.isEmpty() -> false
    isUserAdmin(userMask) -> true.also {
      log.debug { "Access granted: user has ADMIN override" }
    }

    else -> {
      val requiredMask = requiredPermissions.fold(0L) { acc, perm -> acc or perm.bit }
      ((userMask and requiredMask) != 0L).also { hasPerm ->
        log.debug { "ANY permission evaluation result: $hasPerm" }
      }
    }
  }

  fun hasAllPermissions(userMask: Long, vararg requiredPermissions: BitmaskPermission): Boolean = when {
    requiredPermissions.isEmpty() -> false
    isUserAdmin(userMask) -> true
    else -> {
      val requiredMask = requiredPermissions.fold(0L) { acc, perm -> acc or perm.bit }
      ((userMask and requiredMask) == requiredMask).also { hasPerm ->
        log.debug { "ALL permissions evaluation result: $hasPerm" }
      }
    }
  }

  fun grant(currentMask: Long, permissionToAdd: BitmaskPermission): Long =
    (currentMask or permissionToAdd.bit).also { newMask ->
      log.debug { "Granted '$permissionToAdd', old mask: $currentMask, new mask: $newMask" }
    }

  fun revoke(currentMask: Long, permissionToRemove: BitmaskPermission): Long =
    (currentMask and permissionToRemove.bit.inv()).also { newMask ->
      log.debug { "Revoked '$permissionToRemove', old mask: $currentMask, new mask: $newMask" }
    }

  fun getActivePermissionsAsStrings(userMask: Long) = permissionMap
    .filterValues { (userMask and it.bit) != 0L }
    .keys
    .toList()

  fun countEntitiesWithPermission(entities: Iterable<MaskBearer>, permission: BitmaskPermission): Int {
    return entities.count { (it.permissionsMask and permission.bit) != 0L }
  }

  fun formatBitmaskString(permission: BitmaskPermission?): String = when {
    permission == null || permission.bit == 0L -> "0"
    else -> "1 << ${permission.bit.countTrailingZeroBits()}"
  }
}

internal inline fun <reified T> permissionManagerOf(): PermissionManager<T> where T : Enum<T>, T : BitmaskPermission =
  PermissionManager(enumEntries<T>())
