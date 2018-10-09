package com.btp.me.classroom.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.btp.me.classroom.fragment.AssignmentFragment
import com.btp.me.classroom.people.PeopleFragment
import com.btp.me.classroom.slide.SlideFragment

class ClassHomeSelectionAdapter(fm:FragmentManager) :FragmentPagerAdapter(fm) {



    override fun getCount(): Int {
        return 3
    }

    override fun getItem(p0: Int): Fragment? {
        return when(p0){
            0 -> SlideFragment()
            1 -> AssignmentFragment()
            2 -> PeopleFragment()
            else -> null
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when(position){
            0 -> "Slides"
            1 -> "Assignments"
            2 -> "Peoples"
            else -> null
        }
    }

}