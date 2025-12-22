import os
import re

# List of Java files to update with null check
files_to_update = [
    r"d:\RewardsBattle\app-code\app\src\main\java\com\app\rewardsbattle\ui\fragments\WalletFragment.java",
    r"d:\RewardsBattle\app-code\app\src\main\java\com\app\rewardsbattle\ui\activities\AboutUsActivity.java",
    r"d:\RewardsBattle\app-code\app\src\main\java\com\app\rewardsbattle\ui\activities\AnnouncementActivity.java",
    r"d:\RewardsBattle\app-code\app\src\main\java\com\app\rewardsbattle\ui\activities\CustomerSupportActivity.java",
    r"d:\RewardsBattle\app-code\app\src\main\java\com\app\rewardsbattle\ui\activities\HowtoActivity.java",
    r"d:\RewardsBattle\app-code\app\src\main\java\com\app\rewardsbattle\ui\activities\JoiningMatch.java",
    r"d:\RewardsBattle\app-code\app\src\main\java\com\app\rewardsbattle\ui\activities\LeaderboardActivity.java",
    r"d:\RewardsBattle\app-code\app\src\main\java\com\app\rewardsbattle\ui\activities\LotteryActivity.java",
    r"d:\RewardsBattle\app-code\app\src\main\java\com\app\rewardsbattle\ui\activities\MyOrderActivity.java",
    r"d:\RewardsBattle\app-code\app\src\main\java\com\app\rewardsbattle\ui\activities\MyProfileActivity.java",
    r"d:\RewardsBattle\app-code\app\src\main\java\com\app\rewardsbattle\ui\activities\MyReferralsActivity.java",
    r"d:\RewardsBattle\app-code\app\src\main\java\com\app\rewardsbattle\ui\activities\MyStatisticsActivity.java",
    r"d:\RewardsBattle\app-code\app\src\main\java\com\app\rewardsbattle\ui\activities\MyWalletActivity.java",
    r"d:\RewardsBattle\app-code\app\src\main\java\com\app\rewardsbattle\ui\activities\PlayActivity.java",
    r"d:\RewardsBattle\app-code\app\src\main\java\com\app\rewardsbattle\ui\activities\ProductActivity.java",
    r"d:\RewardsBattle\app-code\app\src\main\java\com\app\rewardsbattle\ui\activities\ProductOrderActivity.java",
    r"d:\RewardsBattle\app-code\app\src\main\java\com\app\rewardsbattle\ui\activities\ReferandEarnActivity.java",
    r"d:\RewardsBattle\app-code\app\src\main\java\com\app\rewardsbattle\ui\activities\SelectedGameActivity.java",
    r"d:\RewardsBattle\app-code\app\src\main\java\com\app\rewardsbattle\ui\activities\SelectedResultActivity.java",
    r"d:\RewardsBattle\app-code\app\src\main\java\com\app\rewardsbattle\ui\activities\SingleOrderActivity.java",
    r"d:\RewardsBattle\app-code\app\src\main\java\com\app\rewardsbattle\ui\activities\SingleProductActivity.java",
    r"d:\RewardsBattle\app-code\app\src\main\java\com\app\rewardsbattle\ui\activities\TermsAndConditionActivity.java",
    r"d:\RewardsBattle\app-code\app\src\main\java\com\app\rewardsbattle\ui\activities\TopPlayerActivity.java",
    r"d:\RewardsBattle\app-code\app\src\main\java\com\app\rewardsbattle\ui\activities\TransactionActivity.java",
    r"d:\RewardsBattle\app-code\app\src\main\java\com\app\rewardsbattle\ui\activities\HomeActivity.java",
    r"d:\RewardsBattle\app-code\app\src\main\java\com\app\rewardsbattle\ui\activities\WatchAndEarnActivity.java",
]

def add_null_check(filepath):
    """Add null check to banner loading code"""
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        
        # Pattern to find banner loading without null check
        # Match: RelativeLayout bannerLayout = ...findViewById(R.id.banner_container);
        #        BannerView bannerView = new BannerView(...);
        #        bannerLayout.addView(bannerView);
        #        bannerView.load();
        
        pattern = r'(RelativeLayout bannerLayout = (?:findViewById|root\.findViewById)\(R\.id\.banner_container\);)\s*\n\s*(BannerView bannerView = new BannerView\([^;]+\);)\s*\n\s*(bannerLayout\.addView\(bannerView\);)\s*\n\s*(bannerView\.load\(\);)'
        
        replacement = r'\1\n            if (bannerLayout != null) {\n                \2\n                \3\n                \4\n            }'
        
        content = re.sub(pattern, replacement, content)
        
        # Only write if content changed
        if content != original_content:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"✓ Added null check: {os.path.basename(filepath)}")
            return True
        else:
            print(f"- Already has null check or no banner code: {os.path.basename(filepath)}")
            return False
            
    except Exception as e:
        print(f"✗ Error updating {os.path.basename(filepath)}: {str(e)}")
        return False

def main():
    print("Adding null checks to Unity Ads banner code...\n")
    updated_count = 0
    
    for filepath in files_to_update:
        if os.path.exists(filepath):
            if add_null_check(filepath):
                updated_count += 1
        else:
            print(f"✗ File not found: {filepath}")
    
    print(f"\n{'='*50}")
    print(f"Complete! Added null checks to {updated_count}/{len(files_to_update)} files")
    print(f"{'='*50}")

if __name__ == "__main__":
    main()
