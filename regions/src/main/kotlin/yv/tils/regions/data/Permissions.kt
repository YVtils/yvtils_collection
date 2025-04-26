package yv.tils.regions.data

enum class Permissions(val permission: String) {
    REGION_CREATE("yvtils.command.regions.create"),
    REGION_DELETE("yvtils.command.regions.delete"),
    REGION_INFO("yvtils.command.regions.info"),
    REGION_LIST("yvtils.command.regions.list.generic"),
    REGION_LIST_ROLE("yvtils.command.regions.list.role"),
    REGION_LIST_OTHER("yvtils.command.regions.list.other"),
    REGION_MEMBER("yvtils.command.regions.members.generic"),
    REGION_MEMBER_ROLE("yvtils.command.regions.members.role"),
    REGION_MEMBER_ADD("yvtils.command.regions.members.add"),
    REGION_MEMBER_REMOVE("yvtils.command.regions.members.remove"),
}