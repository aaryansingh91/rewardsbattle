//For set fragment to viewpager via sectionpager adapter
package com.app.rewardsbattle.ui.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.app.rewardsbattle.ui.fragments.EarnFragment;
import com.app.rewardsbattle.ui.fragments.MeFragment;
import com.app.rewardsbattle.ui.fragments.PlayFragment;

public class SectionsPagerAdapter extends FragmentPagerAdapter {


    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new EarnFragment();
            case 1:
                return new PlayFragment();
            case 2:
                return new MeFragment();
            case 3:
                return new MeFragment();
            case 4:
                return new MeFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 5;
    }
}