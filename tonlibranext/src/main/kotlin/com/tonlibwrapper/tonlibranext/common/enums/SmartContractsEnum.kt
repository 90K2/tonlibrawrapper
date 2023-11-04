package com.tonlibwrapper.tonlibranext.common.enums

// sft-wallets stored in wallets with type 'SFT'
// sft-collections stored in `collections` with type 'SFT'

// contracts.type os always either NFT or SFT
//
/**
 * used only for wide contract type detection inside TondataLoader
 */
enum class ContractType {
    NFT,             // nft item
    SFT,             // sft item (sft-minter)
    COLLECTION,  // -- only for TondataLoader
    COLLECTABLE_NFT, // -- only for TondataLoader
    COLLECTABLE_SFT, // -- only for TondataLoader
    CONTRACT;        // other

}

enum class AssetRarity(val n: String) {
    COMMON("Common"),
    UNCOMMON("Uncommon"),
    RARE("Rare"),
    SUPER_RARE("Super rare"),
    EPIC("Epic"),
    LEGENDARY("Legendary");

    companion object {
        fun getByName(name: String?): AssetRarity? {
            return AssetRarity.values().firstOrNull { t-> t.n.lowercase() == name?.lowercase() }
        }
    }
}

enum class SellState {
    ON_SALE, CANCEL, SOLD, TRANSITION_BUY, TRANSITION_SALE, TRANSITION_CANCEL, TRANSITION;

    fun simplify() = if (TRANSITION.name in this.name) TRANSITION else this

    fun inTransition() = simplify() == TRANSITION
}

enum class AssetType {
    SFT, NFT;

    fun isNft() = this == NFT
    fun isSft() = this == SFT
}

// for TonEvents addresses identification
enum class AssetLogicalType {
    COLLECTION, WALLET, SELLER, ASSET
}
