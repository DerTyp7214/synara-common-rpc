package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.rpc.annotations.RestPost
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import dev.dertyp.ui.UiContext
import dev.dertyp.ui.UiContributionInfo
import dev.dertyp.ui.UiContributionKind
import dev.dertyp.ui.UiHomeLayout
import dev.dertyp.ui.UiHookEvent
import dev.dertyp.ui.UiHookHandler
import dev.dertyp.ui.UiInvokePayload
import dev.dertyp.ui.UiInvokeResult
import dev.dertyp.ui.UiLiveUpdate
import dev.dertyp.ui.UiRender
import dev.dertyp.ui.UiSlotRender
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc("Server-driven UI: lists, renders and drives slot, page and home-card contributions from the server and plugins. Text is localized using the Accept-Language header; component trees are shaped to the X-Ui-Schema-Version header. Authorization is enforced per contribution.")
interface IUiService {
    @RpcDoc("List UI contributions visible to the current user.")
    suspend fun listContributions(
        @RpcParamDoc("Filter by kind.") kind: UiContributionKind? = null,
        @RpcParamDoc("Filter by slot name.") slot: String? = null,
    ): List<UiContributionInfo>

    @RestPost
    @RpcDoc("Render all contributions registered for a slot that the current user may see, in display order.")
    suspend fun renderSlot(
        @RpcParamDoc("Slot name, see UiSlots.") slot: String,
        @RpcParamDoc("Host context of the slot.") context: UiContext,
    ): UiSlotRender

    @RestPost
    @RpcDoc("Render a single contribution.", errors = ["IllegalArgumentException", "UnauthorizedException"])
    suspend fun render(
        @RpcParamDoc("Contribution id.") contributionId: String,
        @RpcParamDoc("Host context.") context: UiContext,
    ): UiRender

    @RpcDoc("Stream re-renders of a contribution. Emits the current render immediately, then on every change.", errors = ["IllegalArgumentException", "UnauthorizedException"])
    fun subscribe(
        @RpcParamDoc("Contribution id.") contributionId: String,
        @RpcParamDoc("Host entity id, if the contribution is rendered on an entity screen.") entityId: PlatformUUID? = null,
    ): Flow<UiRender>

    @RpcDoc("Stream updates for a Live node of a contribution. Subscribe once per Live node while it is on screen.", errors = ["IllegalArgumentException", "UnauthorizedException"])
    fun subscribeLive(
        @RpcParamDoc("Contribution id.") contributionId: String,
        @RpcParamDoc("Key of the Live node.") key: String,
        @RpcParamDoc("Host entity id, if the contribution is rendered on an entity screen.") entityId: PlatformUUID? = null,
    ): Flow<UiLiveUpdate>

    @RestPost
    @RpcDoc("Dispatch an action declared by a contribution, with form values and/or action parameters.", errors = ["IllegalArgumentException", "UnauthorizedException"])
    suspend fun invoke(
        @RpcParamDoc("Contribution id.") contributionId: String,
        @RpcParamDoc("Action id from UiAction.Invoke.") actionId: String,
        @RpcParamDoc("Values and context.") payload: UiInvokePayload,
    ): UiInvokeResult

    @RestPost
    @RpcDoc("Forward an app-level event (e.g. a shared URL). Returns every contribution offering to handle it; perform the single action directly or let the user choose. An empty list means no server-side handler; fall back to native behaviour.")
    suspend fun dispatchHook(
        @RpcParamDoc("The event.") event: UiHookEvent,
    ): List<UiHookHandler>

    @RpcDoc("Home-card layout of the current user, including cards available for pinning.")
    suspend fun getHomeCards(): UiHomeLayout

    @RpcDoc("Pin or unpin a home card for the current user.", errors = ["IllegalArgumentException"])
    suspend fun setHomeCardPinned(
        @RpcParamDoc("HOME_CARD contribution id.") contributionId: String,
        @RpcParamDoc("Whether the card should be pinned.") pinned: Boolean,
    ): UiHomeLayout

    @RpcDoc("Persist the order of the current user's pinned home cards.")
    suspend fun setHomeCardOrder(
        @RpcParamDoc("Pinned contribution ids in display order.") contributionIds: List<String>,
    ): UiHomeLayout

    @RpcDoc("Stream the current user's home-card layout.")
    fun getHomeCardsFlow(): Flow<UiHomeLayout>
}
