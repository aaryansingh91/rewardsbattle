import os
import re

# List of Java files to update
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
]

def update_file(filepath):
    """Update a single Java file to use Unity Ads instead of Google Ads"""
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        
        # Remove Google Ads imports
        content = re.sub(r'import com\.google\.android\.gms\.ads\.AdListener;\s*\n', '', content)
        content = re.sub(r'import com\.google\.android\.gms\.ads\.AdRequest;\s*\n', '', content)
        content = re.sub(r'import com\.google\.android\.gms\.ads\.AdView;\s*\n', '', content)
        content = re.sub(r'import com\.google\.android\.gms\.ads\.LoadAdError;\s*\n', '', content)
        content = re.sub(r'import com\.google\.android\.gms\.ads\.MobileAds;\s*\n', '', content)
        
        # Add Unity Ads imports (check if not already present)
        if 'import com.unity3d.services.banners.BannerView;' not in content:
            # Find the last import statement
            last_import_match = list(re.finditer(r'^import .*?;', content, re.MULTILINE))
            if last_import_match:
                insert_pos = last_import_match[-1].end()
                unity_imports = '\nimport com.unity3d.services.banners.BannerView;\nimport com.unity3d.services.banners.UnityBannerSize;\nimport android.widget.RelativeLayout;'
                content = content[:insert_pos] + unity_imports + content[insert_pos:]
        
        # Determine if this is a Fragment or Activity
        is_fragment = 'Fragment' in filepath
        context_ref = 'requireActivity()' if is_fragment else 'this'
        find_view_ref = 'root.findViewById' if is_fragment else 'findViewById'
        
        # Replace AdView initialization pattern
        # Pattern: AdView mAdView = findViewById(R.id.adView);
        #          AdRequest adRequest = new AdRequest.Builder().build();
        #          mAdView.loadAd(adRequest);
        #          mAdView.setAdListener(new AdListener() { ... });
        
        # Find and replace the ad loading block
        ad_block_pattern = r'AdView mAdView = ' + re.escape(find_view_ref) + r'\(R\.id\.adView\);[\s\S]*?mAdView\.setAdListener\(new AdListener\(\) \{[\s\S]*?\}\);'
        
        replacement = f'''RelativeLayout bannerLayout = {find_view_ref}(R.id.banner_container);
            BannerView bannerView = new BannerView({context_ref}, getString(R.string.unity_banner_id), new UnityBannerSize(320, 50));
            bannerLayout.addView(bannerView);
            bannerView.load();'''
        
        content = re.sub(ad_block_pattern, replacement, content)
        
        # Also handle MobileAds.initialize if present
        content = re.sub(r'MobileAds\.initialize\(this, initializationStatus -> \{\s*\}\);[\s]*\n', '', content)
        
        # Only write if content changed
        if content != original_content:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"✓ Updated: {os.path.basename(filepath)}")
            return True
        else:
            print(f"- Skipped (no changes): {os.path.basename(filepath)}")
            return False
            
    except Exception as e:
        print(f"✗ Error updating {os.path.basename(filepath)}: {str(e)}")
        return False

def main():
    print("Starting Unity Ads migration for Java files...\n")
    updated_count = 0
    
    for filepath in files_to_update:
        if os.path.exists(filepath):
            if update_file(filepath):
                updated_count += 1
        else:
            print(f"✗ File not found: {filepath}")
    
    print(f"\n{'='*50}")
    print(f"Migration complete! Updated {updated_count}/{len(files_to_update)} files")
    print(f"{'='*50}")

if __name__ == "__main__":
    main()
