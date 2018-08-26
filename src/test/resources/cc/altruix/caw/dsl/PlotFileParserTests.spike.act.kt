import cc.altruix.caw.dsl.v1.Scene
import cc.altruix.caw.dsl.v1.Character
import cc.altruix.caw.dsl.v1.Place
import cc.altruix.caw.dsl.v1.GroupOfCharacters

fun Universe() {

val c_BorisDoctor = Character("c_BorisDoctor", "Boris' doctor in the mental institution")
val c_alexTheProphet = Character("c_alexTheProphet", "Alex the Prophet")
val c_borisTheBadger = Character("c_borisTheBadger", "Boris the Badger")
val c_chiefOfIntelligence = Character("c_chiefOfIntelligence", "Chief of intelligence")
val c_deathman = Character("c_deathman", "Deathman")
val c_egregorius = Character("c_egregorius", "Egregorius")
val c_egregoriusRival = Character("c_egregoriusRival", "Egregorius' rival")
val c_gernot = Character("c_gernot", "Gernot")
val c_jester1 = Character("c_jester1", "Jester 1")
val c_jester2 = Character("c_jester2", "Jester 2")
val c_jester3 = Character("c_jester3", "Jester 3")
val c_jetDragon = Character("c_jetDragon", "Jet dragon")
val c_mageScientist = Character("c_mageScientist", "Scientist (mage)")
val c_mageSurgeon = Character("c_mageSurgeon", "Brain surgeon (mage)")
val c_magicalCreature = Character("c_magicalCreature", "Magical creature")
val c_michaelTheMonkey = Character("c_michaelTheMonkey", "Michael the Monkey")
val c_nibelungElder1 = Character("c_nibelungElder1", "Nibelung Elder #1")
val c_nibelungElder2 = Character("c_nibelungElder2", "Nibelung Elder #1")
val c_nibelungResistanceLeader1 = Character("c_nibelungResistanceLeader1", "Nibelung resistance leader #1")
val c_nibelungResistanceLeader2 = Character("c_nibelungResistanceLeader2", "Nibelung resistance leader #2")
val c_nibelungResistanceLeader3 = Character("c_nibelungResistanceLeader3", "Nibelung resistance leader #3")
val c_pilot = Character("c_pilot", "Pilot")
val c_siegfried = Character("c_siegfried", "Siegfried")
val c_spy = Character("c_spy", "Cleanlands spy")
val c_valkyrie = Character("c_valkyrie", "Valkyrie")
val cg_cleanlandsPrisoners = GroupOfCharacters("cg_cleanlandsPrisoners", "Captivated Cleanlanders")
val cg_cleanlandsSoldiers1 = GroupOfCharacters("cg_cleanlandsSoldiers1", "Cleanlands' soldiers (1)")
val cg_cleanlandsSoldiers2 = GroupOfCharacters("cg_cleanlandsSoldiers2", "Cleanlands' soldiers (2)")
val cg_cleanlandsSoldiers3 = GroupOfCharacters("cg_cleanlandsSoldiers3", "Cleanlands' soldiers (3)")
val cg_cleanlandsSoldiers4 = GroupOfCharacters("cg_cleanlandsSoldiers4", "Cleanlands' soldiers (4)")
val cg_cleanlandsSoldiers5 = GroupOfCharacters("cg_cleanlandsSoldiers5", "Cleanlands' soldiers (5)")
val cg_cleanlandsSoldiers6 = GroupOfCharacters("cg_cleanlandsSoldiers6", "Cleanlands' soldiers (6)")
val cg_councilMembers = GroupOfCharacters("cg_councilMembers", "Mage council members")
val cg_intelligenceStaff = GroupOfCharacters("cg_intelligenceStaff", "Nibelungo-Cleanlandsian\nintelligence staff")
val cg_miners = GroupOfCharacters("cg_miners", "Nibelung miners")
val cg_nibelungCorruptionists = GroupOfCharacters("cg_nibelungCorruptionists", "Nibelung corruptionists")
val cg_nibelungCourtiers = GroupOfCharacters("cg_nibelungCourtiers", "Nibelung courtiers")
val cg_nibelungCrowd = GroupOfCharacters("cg_nibelungCrowd", "Nibelung crowd")
val cg_nibelungLeaders = GroupOfCharacters("cg_nibelungLeaders", "Nibelung leaders")
val cg_nibelungMilitary = GroupOfCharacters("cg_nibelungMilitary", "Nibelung soldiers\nand officers")
val cg_nibelungPrisoners = GroupOfCharacters("cg_nibelungPrisoners", "Captivated Nibelungs")
val cg_valkyriesRelatives = GroupOfCharacters("cg_valkyriesRelatives", "Valkyrie's relatives")
val p_academy = Place("p_academy", "Castalian Academy of Fine Arts")
val p_border = Place("p_border", "Border between Nibelungia and Cleanlands")
val p_borisDen = Place("p_borisDen", "Boris's den")
val p_centralSquare = Place("p_centralSquare", "Central square of capitol city of Nibelungia")
val p_cleanlandsCapital = Place("p_cleanlandsCapital", "Capital city of CleanLands")
val p_corruptionistsMansion = Place("p_corruptionistsMansion", "Corruptionists' mansion")
val p_flying_island = Place("p_flying_island", "Flying island")
val p_forgottenCastle = Place("p_forgottenCastle", "Forgotten castle")
val p_magesBunker = Place("p_magesBunker", "Mages' bunker\nCheyenne mountain")
val p_mentalInstitution = Place("p_mentalInstitution", "Mental institution")
val p_mines = Place("p_mines", "Mines")
val p_nibelungKingsResidence = Place("p_nibelungKingsResidence", "Nibelung King's residence")
val p_nibelungianGovtBuilding = Place("p_nibelungianGovtBuilding", "Nibelungian government building")
val p_outcasts = Place("p_outcasts", "Land of the outcasts\n101st kilometer")
val p_outcasts_dragon_training_ground = Place("p_outcasts_dragon_training_ground", "Dragon training ground")
val p_outcasts_tavern = Place("p_outcasts_tavern", "Tavern")
val p_residenceOfNibelungResistanceLeader1 = Place("p_residenceOfNibelungResistanceLeader1", "Residence of Nibelung resistance leader #1")
val p_residenceOfNibelungResistanceLeader2 = Place("p_residenceOfNibelungResistanceLeader2", "Residence of Nibelung resistance leader #2")
val p_residenceOfNibelungResistanceLeader3 = Place("p_residenceOfNibelungResistanceLeader3", "Residence of Nibelung resistance leader #3")
val p_simulacrumControlCenter = Place("p_simulacrumControlCenter", "Simulacrum control center")
val p_spaceBetweenForgottenCastleAndFlyingIsland = Place("p_spaceBetweenForgottenCastleAndFlyingIsland", "Space between\nforgotten castle and\nflyingIsland")
val p_spiritualPlaceOfNibelungs = Place("p_spiritualPlaceOfNibelungs", "Spiritual place of the Nibelungs")
val p_spyMinistry = Place("p_spyMinistry", "Spy ministry")
val p_valkyriesHomeCastle = Place("p_valkyriesHomeCastle", "Valkyrie's home castle")
val sc1 = Scene("sc1", "1: S. feels he loves V.")


val sc2 = Scene("sc2", "2: Siegfried and Valkyrie meet each other for the first time")


val sc3 = Scene("sc3", "3: S. writes a letter to V.'s home castle")


val sc4 = Scene("sc4", "4: S. doesn't receive an answer")


val sc5 = Scene("sc5", "5: S. arrives in Nibelungia's capital city")
sc5.addNote("""

Scene 5: Siegfried arrives in capital city of Nibelungia

""")


val sc6 = Scene("sc6", "6: S. visits V's home castle and finds it devastated")
sc6.addNote("""

Scene 6: S. visits V's home castle and finds it devastated

""")


val sc7 = Scene("sc7", "7: S. visits the Academy")
sc7.addNote("""

Scene 7: S. visits the Academy

""")


val sc8 = Scene("sc8", "8: S. sees degradation of Nibelungia")
sc8.addNote("""

Scene 8: S. sees degradation of Nibelungia

""")


val sc9 = Scene("sc9", "9: S. thinks whether or not\nto become a propaganda poet")
sc9.addNote("""

Scene 9: S. thinks whether or not\nto become a propaganda poet

""")


val sc10 = Scene("sc10", "10: S. goes to the mines")
sc10.addNote("""

Scene 10: S. goes to the mines

""")


val sc11 = Scene("sc11", "11: S. learns about the magic powers of a magic diamond")
sc11.addNote("""

Scene 11: S. learns about the magic powers of a magic diamond

""")


val sc12 = Scene("sc12", "12: S. starts to search for the diamond")
sc12.addNote("""

Scene 12: S. starts to search for the diamond

""")


val sc13 = Scene("sc13", "13: S. gets caved in")
sc13.addNote("""

Scene 13: S. gets caved in

""")


val sc14 = Scene("sc14", "14: S. finds the diamond")
sc14.addNote("""

Scene 14: S. finds the diamond

""")


val sc15 = Scene("sc15", "15: S. gets into the land of the outcasts\n101st kilometer")
sc15.addNote("""

Scene 15: S. gets into the land of the outcasts

""")


val sc16 = Scene("sc16", "16: S. finds a treasure chest with gold")
sc16.addNote("""

Scene 16: S. finds a treasure chest with gold

""")


val sc17 = Scene("sc17", "17: S. goes to CleanLands")
sc17.addNote("""

Scene 17: S. goes to CleanLands

""")


val sc18 = Scene("sc18", "18: S. can't find V. in Cleanlands")
sc18.addNote("""

Scene 18: S. can't find V. in Cleanlands

""")


val sc19 = Scene("sc19", "19: S. returns to the outcasts")
sc19.addNote("""

Scene 19: S. returns to the outcasts

""")


val sc20 = Scene("sc20", "20: S. gets to know the pilot in a tavern")
sc20.addNote("""

Scene 20: S. gets to know the pilot in a tavern

""")


val sc21 = Scene("sc21", "21: S. visits the dragon training place")
sc21.addNote("""
21: S. visits the dragon training place
""")


val sc22 = Scene("sc22", "22: S. goes with the pilot to the flying island")
sc22.addNote("""
22: S. goes with the pilot to the flying island
""")


val sc23 = Scene("sc23", "23: S. meets V. on the flying island")
sc23.addNote("""
23: S. meets V. on the flying island
""")


val sc24 = Scene("sc24", "24: S. and pilot get attacked upon\nthe return to the land of outcasts")
sc24.addNote("""
24: S. and pilot get attacked upon\nthe return to the land of outcasts
""")


val sc25 = Scene("sc25", "25: S. and pilot find a forgotten castle")
sc25.addNote("""
25: S. and pilot find a forgotten castle
""")


val sc26 = Scene("sc26", "26: S. takes care of the wounded pilot")
sc26.addNote("""
26: S. takes care of the wounded pilot
""")


val sc27 = Scene("sc27", "27: S. goes to the flying island,\nmeets a magical creature")
sc27.addNote("""
27: S. goes to the flying island,\nmeets a magical creature
""")


val sc28 = Scene("sc28", "28: S. and V. come back to forgotten castle")
sc28.addNote("""
28: S. and V. come back to forgotten castle
""")


val sc29 = Scene("sc29", "29: S. and V. spend a romantic evening")
sc29.addNote("""
29: S. and V. spend a romantic evening
""")


val sc30 = Scene("sc30", "30: V. discovers that S.\nwrote a poem with his blood")
sc30.addNote("""
30: V. discovers that S.\nwrote a poem with his blood
""")


val sc31 = Scene("sc31", "31: V. urges S. to go to Nibelungia\nand use his connections to\nspread the poem")
sc31.addNote("""
31: V. urges S. to go to Nibelungia\nand use his connections to\nspread the poem
""")


val sc32 = Scene("sc32", "32: Pilot teaches S. to fight")
sc32.addNote("""
32: Pilot teaches S. to fight
""")


val sc33 = Scene("sc33", "33: Pilot gets sick before\nplanned journey to Nibelungia")
sc33.addNote("""
33: Pilot gets sick before\nplanned journey to Nibelungia
""")


val sc34 = Scene("sc34", "34: S. starts journey to\nNibelungia with the jet dragon")
sc34.addNote("""
34: S. starts journey to\nNibelungia with the jet dragon
""")


val sc35 = Scene("sc35", "35: S. meets Gernot and gives\nhim a copy of the poem")
sc35.addNote("""
35: S. meets Gernot and gives\nhim a copy of the poem
""")


val sc36 = Scene("sc36", "36: S. gets arrested")
sc36.addNote("""
36: S. gets arrested
""")


val sc37 = Scene("sc37", "37: Talk between S. and Egregorius")


val sc38 = Scene("sc38", "38: S. gets at the scaffold")


val sc39 = Scene("sc39", "39: While E. is at S's execution,\nE's rival and Gernot put\nthe poem on the simulacri broadcast")


val sc40 = Scene("sc40", "40: In the last moment, the crowd\nsaves S. from execution and\nkills E. by rolling a giant Simulacrum")


val sc41 = Scene("sc41", "41: V. gets nervous about S.'s\nabsence and starts journey\nto N.'s capital city with the pilot")


val sc42 = Scene("sc42", "42: V. arrives in N.'s capital city")


val sc43 = Scene("sc43", "43: V. sees S. surrounded\nby many young women")


val sc44 = Scene("sc44", "44: S. approaches V., V. faints")


val sc45 = Scene("sc45", "45: V. wakes up at the forgotten castle,\nlearns she is pregnant, S. looks\ninto ростки of bright future through\nподзорная труба")


val sc46 = Scene("sc46", "46: E. realizes that cold war didn't\nkill N., only made them stronger.")
sc46.addNote("""

The mages control the world right now and want to continue doing so in future.
In order to do so, all of the governed species must be controlled/tamed by them.
The people of Cleanlands can be controlled by the mages and therefore the life there
is better than in Nibelungia.

The mages tried to conquer/destroy Nibelungia several times and always failed.
Now they try to destroy it using non-military means.

They want to destroy Nibelungia because for some reason, Nibelungians are less prone to
their propaganda than the people of Cleanlands. The mages fear that if the Nibelungians
are not killed

Show the problem. The problem is that the Nibelungs are getting out of control.
Or - the cold war didn't work out, really.

""")


val sc47 = Scene("sc47", "E. attends a seminar on complimentarity of Nibelungs and Cleanlands")
sc47.addNote("""

Egregorius attends a seminar, where he learns that Nibelung and Cleanlands' minds are complimentary,
i. e. by forming a group with Nibelungs and Cleanlandsians, the group is much more likely to achieve
scientific breakthroughs than any of them alone.

This means: Together N. and Cleanlands

""")


val sc48 = Scene("sc48", "E. attends meeting regarding\nproposal of Cleanlands' intellectuals\nto unite with Nibelungia")
sc48.addNote("""

Several Cleanlands intellectuals propose to the government of Cleanlands to bury the hatchet
and unite Cleanlands and Nibelungia into one state, so that resources that go into 
warfare are put into art and science instead.

""")


val sc49 = Scene("sc49", "Confrontation between E. and his rival")


val sc50 = Scene("sc50", "Nibelungian leaders discuss how to\njoin Cleanlands on acceptable terms")
sc50.addNote("""

Meeting between Nibelungian elders. They discuss, how to join Cleanlands on acceptable (for the Nibelungs)
terms. Two leaders - N. elder 1 and N. elder 2.

They agree on the following plan:

1) First, they give rise to a hard-core, hawkish leader.
2) He frightens the elite of the Cleanlands.
3) Then, they replace the hawkish leader with a more pleasant/peaceful one.

Since both of them are just executors of a bigger plans, they must not have their own intelligence.
The hawkish and the peaceful leader must be something like trained monkeys.

But as stupid as they are, both need additional training. The hawkish one needs to get to know weapons,
strategy and the secrets of Nibelungo-Cleanlandsian politics.

The stupid one needs to be indoctrinated with peaceful ideology.

""")


val sc51 = Scene("sc51", "Nibelung elder 2 meets with Nibelung corruptionists,\nproposes to subvert the plan of N. elder 1")
sc51.addNote("""

Nibelung elite wants to change the regime. It doesn't want to live like they used. They want independence
and liberty to steal as much as they can.

If Nibelungia joins Cleanlands, they will become lesser in ranks than now. If Cleanlands destroy the state
system of Nibelungia, they can steal much more money than now.

""")


val sc52 = Scene("sc52", "Nibelung elder 2 tells to a Cleanlands spy about the plan.")


val sc53 = Scene("sc53", "Egregorius meets the spy")


val sc54 = Scene("sc54", "Egregorius' speech before the council")
sc54.addNote("""

There is an ongoing discussion on whether Cleanlands should leave N. in peace, or attack it.
After the spy's message, Egregorius has an argument in favor of hard-core line:

1) If the mages don't intervene and the plan of the Nibelung elders works out, Nibelung and Cleanlands 
people will peacefully unite. In this case Nibelung culture will modify Cleanlands mindset and together
Nibelungo-Cleanlandsian intellectuals will achieve the same level of intellect (discover the secrets,
which now only the mages know) as the mages. Then, they may decide to govern themselves without the mages.

2) The hawkish leader may attack Cleanlands to prove he means business. Or he may provide the Barbarians
with some technology they can use to attack Cleanlands.

He proposes to subvert the hawkish-peaceful leader plan with the help of Nibelung elite.

""")


val sc55 = Scene("sc55", "Nibelung elder 1 dies")


val sc56 = Scene("sc56", "Nibelung courtiers prepare the pilot for coronation")
sc56.addNote("""

Now after the death of Nibelung elder #1, the hawkish leader (the pilot) is supposed to get into power.

""")


val sc57 = Scene("sc57", "Cleanlands special troops (helicopter?) tries to kill the pilot")
sc57.addNote("""

Twist: Maybe the pilot is at the Valkyrie's home castle before coronation. When he is assassianted,
the Cleanlands spy burn down the castle (to make sure he is really dead).

""")


val sc58 = Scene("sc58", "The pilot escapes into the land of the outcasts")


val sc59 = Scene("sc59", "Coronation of Michael the Monkey")


val sc60 = Scene("sc60", "Cleanlandsian king visits Nibelungia, awards an order to Michael the Monkey")


val sc61 = Scene("sc61", "Michael the Monkey is happy about the order")
sc61.addNote("""

Michael the Monkey is happy about the order. Jumps and screams from happiness.

""")


val sc62 = Scene("sc62", "Second meeting of the corruptionists")
sc62.addNote("""

Nibelung elder 2 meets with Nibelung corruptionists the second time.

They need a new "leader", who will make Nibelungia a Gau of the Cleanlands and allow them to steal more.
Problem: Michael the Monkey is still too intelligent. They need someone with the brain of a rat.

Yeltsin played with a grenade and tried to open it using a hammer.

Margaret Thatcher is also a trained monkey, a cardboard president or robot.

Same for Winston Churchill

Nibelung elders may even take this tradition of carboard presidents from the mages. 
The mages started it first.

""")


val sc63 = Scene("sc63", "The corruptionists discover Boris the Rat")


val sc64 = Scene("sc64", "The corruptionists install a rat's brain\ninto Boris' head, transform him into\na remote-controlled zombie")


val sc65 = Scene("sc65", "Nibelung elder 2's meeting with the jesters")
sc65.addNote("""

Parade of jesters

Nibelung elder 2 meets with several jesters. They are supposed to entertain the crowd, when the
Cleanlands troops march in.

""")


val sc66 = Scene("sc66", "Introduction of Alex the Prophet")
sc66.addNote("""

Alex the prophet
His purpose is to intellectually justify the occupation of Nibelungia. He is a respected
Nibelungian, a fighter with the regime.

""")


val sc67 = Scene("sc67", "Cleanlands troops march into\nNibelungia's capital city.\nBoris the ? receives them.")
sc67.addNote("""

Preparation for invasion

""")


val sc68 = Scene("sc68", "Boris the ? goes with the\nCleanlands soldiers to the residence\nof Michael the Monkey and kills him")


val sc69 = Scene("sc69", "Boris the ? disbands Nibelung military")


val sc70 = Scene("sc70", "Boris the ? installs the giant Simulacrum\nin the center of Nibelungia's capital city")
sc70.addNote("""

Scene 70 can be a great show, like the concert after the Putsch in 1991. Jesters and Alex the prophet tell

1) how great the transition to the Cleanlands way of life is, and 
2) the Nibelungs have been doing all wrong.

""")


val sc71 = Scene("sc71", "People in the mines see a moving picture\nin the simulacrum about Nibelungs being\nrenamed to Nibelungians")
sc71.addNote("""

Purpose of this scene is to show the decay of everyday life after the invasion.

""")


val sc72 = Scene("sc72", "Mages celebrate Nibelungia's annexion")
sc72.addNote("""

Celebration of Nibelungia's defeat in the council of the mages
sc72: Place is the former NORAD bunker in the Cheyenne mountain, which now is more a memorial place.

Purpose of the scene is to discuss the results of the invasion:

1) Military defeat - now Nibelungia simply doesn't have a military.
2) Intellectual defeat - 99 % of Nibelungia's intellectuals have either emigrated to Cleanlands (where
they just enjoy life and/or struggle for survival), or work in Nibelungian mines and devote their 
brainpower to mining as much coal as possible.

""")


val sc73 = Scene("sc73", "Egregorius observes life in\nNibelungia to find out,\nhow to finally kill it")
sc73.addNote("""

Several years after the invasion, Nibelungia shows no signs of death. The society still lives and
the decay of the population has stopped. The Nibelungs have adapted themselves to the harsh conditions
of the mines.

""")


val sc74 = Scene("sc74", "Egregorius thinks about\nhow to incite an uprising\nin Nibelungia")
sc74.addNote("""

Egregorius talks with a colleague, who was responsible for putting Cleanlands under control.
He proposes to provoke Nibelungians to an uprising, then apply weapons of mass destruction to kill
all of them with one strike. This scheme has been applied in various Gaus of the Cleanlands and even
to Barbarians.

Egregorius thinks about how to incite an uprising.

""")


val sc75 = Scene("sc75", "Valkyrie is affected by anti-women\npolicies, decides to flee to Cleanlands")
sc75.addNote("""

Egregorius initiates a set of measures targeted against Nibelung women. Its purpose is to make the
Nibelungs revolt.

""")


val sc76 = Scene("sc76", "Fight between E. and his rival at the Council meeting")
sc76.addNote("""

Fight between E. and his rival. The latter accuses E. of recommending the wrong strategy.
Because E. wanted to go hard-core, the mages did and - despite the expectations - Nibelungia
did not crumble. Sure, the life there isn't as good as in Cleanlands, but Nibelungia doesn't
show signs of fast death. The population growth plunged during the first years, but now
it's recovering and no amount of brainkilling jester propaganda could bring Nibelungia 
down completely.

E's rival proposes to kill Nibelungia by not trying to kill it, by a real detente, which
will make Nibelungs lazy and thus guarantee a slow, but sure degradation.

E says that the council shoul decide. They should vote in several weeks on who is to become
the leader of the future Nibelung Gau.

The council and E's rival agree.

""")


val sc77 = Scene("sc77", "E. talks with members of the Council")
sc77.addNote("""

E. talks with members of the council in an attempt to convince them that his line is the right one.

""")


val sc79 = Scene("sc79", "Boris the ? dies")
sc79.addNote("""

Boris the ? dies and/or is killed.

""")


val sc80 = Scene("sc80", "E. becomes leader of the Nibelung Gau")


val sc81 = Scene("sc81", "Meeting of E. with the\nchief of intelligence")
sc81.addNote("""

E. discusses with the chief of intelligence of the mages, what to do about the 
(probably existing) underground resistance of the Nibelungs.

1) The attempt at inciting an uprising by hurting women failed. There was no revolution, nothing.
2) No viable resistance has formed despite the fact that Boris the ? destroyed almost every aspect of life. 
Forcing people to the mines did not upset them!
3) E. asks the chief of intelligence to investigate as thoroughly as possible, who may be the leaders of the resistance.
4) Also, E. asks him to find out what happened to the Nibelung elders.
5) He (chief of intelligence) argues that if they existed, he would know.
6) E. says the Nibelungs may have developed a new form of magic. He explains to the chief of intelligence
the legend about soul transfer and that higher powers protected Nibelungia.
7) Chief of intelligence promises to investigate these rumors.

""")


val sc82 = Scene("sc82", "Chief of inteligence talks\nwith Nibelungian resistance leaders")


val sc82a = Scene("sc82a", "Chief of inteligence talks\nwith Nibelungian resistance leader #1")


val sc82b = Scene("sc82b", "Chief of inteligence talks\nwith Nibelungian resistance leader #2")


val sc82c = Scene("sc82c", "Chief of inteligence talks\nwith Nibelungian resistance leader #3")


val sc83 = Scene("sc83", "E. talks the chief of\nintelligence again, urges him\nto find roots of uprising")


val sc84 = Scene("sc84", "Chief of intelligence\nasks his people to find\nsomeone, who looks like a\nrebel to make E. happy")


val sc85 = Scene("sc85", "E. says to Chief of intelligence that S. is proof that an uprising is boiling in N.")


val sc91f = Scene("sc91f", "Birth of Egregorius (show, what relationships look like at the mages)")
sc91f.addNote("""

Birth of Egregorius (show, what relationships look like at the mages)

""")


val sc94f = Scene("sc94f", "Cleanlands and or mage elite reps electrocute Michael the Monkey")
sc94f.addNote("""

Cleanlands and or mage elite reps electrocute Michael the Monkey from 
the insane asylum, until he gets dumb enough and has a mark on his head

""")


val sc95f = Scene("sc95f", "Valkyrie is in a hut of a witch")
sc95f.addNote("""

Valkyrie is in a hut of a witch, who puts a protective ring on her, 
which protects her from the danger and – as a side effect – makes it 
harder for her to fall in love. They watch the news from 
Nibelungia's capital city together, then Valkyrie decides, she is safe to go now.

""")


val sc96f = Scene("sc96f", "Siegfried receives degree as a poet,\ngoes to Cleanlands for an internship")
sc96f.addNote("""

Siegfried receives degree as a poet, goes to Cleanlands for an internship

""")


val sc97f = Scene("sc97f", "Siegfried sees, how people live in Cleanlands")
sc97f.addNote("""

Siegfried sees, how people live in Cleanlands

""")


val sc98f = Scene("sc98f", "Siegfried gets involved in an intellectual debate")
sc98f.addNote("""

Siegfried gets involved in an intellectual debate with high-end Cleanlands 
students in a cafè, while learning about the change in Nibelungia

""")


val sc99f = Scene("sc99f", "Siegfried's internship is over,\nhe starts journey to Nibelungia")
sc99f.addNote("""

Siegfried's internship is over, he starts journey to Nibelungia

""")


val sc100f = Scene("sc100f", "Valkyrie's sufferings on the road to\nCleanlands and in Cleanlands.\nAccession to the flying island.")
sc100f.addNote("""

Valkyrie's sufferings on the road to Cleanlands and in Cleanlands. Accession to the flying island.

""")


val sc101f = Scene("sc101f", "Flashback to scene 38, the reader restores the context here")
sc101f.addNote("""

Flashback to scene 38, the reader restores the context here

""")




}