package tronador

object Entities {
  sealed trait Entity {
    def isSafe: Boolean
  }

  sealed trait EnemyEntity extends Entity {
    override def isSafe: Boolean = false
  }

  sealed trait FriendEntity extends Entity {
    override def isSafe: Boolean = true
  }

  case object EnemyMaster extends EnemyEntity
  case object EnemySlave extends EnemyEntity
  case object EnemyBeast extends EnemyEntity
  case object EnemyPlant extends EnemyEntity

  case object FriendMaster extends FriendEntity
  case object FriendSlave extends FriendEntity
  case object FriendPlant extends FriendEntity
  case object FriendBeast extends FriendEntity

  case object Wall extends Entity {
    override def isSafe: Boolean = false
  }
  case object Empty extends Entity {
    override def isSafe: Boolean = true
  }
  case object Unknown extends Entity {
    override def isSafe: Boolean = true
  }

  val ENEMY_MASTER = 'm'
  val ENEMY_SLAVE = 's'
  val ENEMY_BEAST = 'b'
  val ENEMY_PLANT = 'p'

  val FRIEND_MASTER = 'M'
  val FRIEND_SLAVE = 'S'
  val FRIEND_PLANT = 'P'
  val FRIEND_BEAST = 'B'

  val WALL = 'W'
  val EMPTY = '_'
  val UNKNOWN = '?'

  def valueOf(c: Char): Entity = c match {
    case ENEMY_MASTER => EnemyMaster
    case ENEMY_SLAVE => EnemySlave
    case ENEMY_BEAST => EnemyBeast
    case ENEMY_PLANT => EnemyPlant
    case FRIEND_MASTER => FriendMaster
    case FRIEND_SLAVE => FriendSlave
    case FRIEND_PLANT => FriendPlant
    case FRIEND_BEAST => FriendBeast
    case WALL => Wall
    case EMPTY => Empty
    case UNKNOWN => Unknown
  }
}
