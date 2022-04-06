package com.example.demo.graphql.db;

import com.example.demo.graphql.model.SuperHero;
import com.example.demo.graphql.model.Team;

import javax.enterprise.context.ApplicationScoped;
import java.util.*;

@ApplicationScoped
public class HeroDatabase {
    private final Map<String, SuperHero> allHeroes = new HashMap<>();
    private final Map<String, Team> allTeams = new HashMap<>();

    public SuperHero getHero(String name) throws UnknownHeroException {
        SuperHero superHero = allHeroes.get(name);

        if (superHero == null) {
            throw new UnknownHeroException(name);
        }

        return superHero;
    }

    public Team getTeam(String name) throws UnknownTeamException {
        Team team = allTeams.get(name);
        if (team == null) {
            throw new UnknownTeamException(name);
        }
        return team;
    }

    public Collection<SuperHero> getAllHeroes() {
        return allHeroes.values();
    }

    public Collection<Team> getAllTeams() {
        return allTeams.values();
    }

    public int addHeroes(Collection<SuperHero> heroes) {
        int count = 0;
        for (SuperHero hero : heroes) {

            addHero(hero);
            count++;

        }
        return count;
    }

    public void addHero(SuperHero hero) {
        allHeroes.put(hero.getName(), hero);
        List<Team> teams = hero.getTeamAffiliations();
        if (teams != null) {
            ListIterator<Team> iter = teams.listIterator();
            while (iter.hasNext()) {
                Team team = iter.next();
                Team existingTeam = allTeams.get(team.getName());
                if (existingTeam == null) {
                    existingTeam = createNewTeam(team.getName());
                }
                iter.set(existingTeam);
                List<SuperHero> members = existingTeam.getMembers();
                if (members == null) {
                    members = new ArrayList<>();
                    existingTeam.setMembers(members);
                }
                members.add(hero);
            }
        }
    }

    public SuperHero removeHero(String heroName) {
        SuperHero hero = allHeroes.remove(heroName);
        if (hero == null) {
            return null;
        }
        for (Team team : getAllTeams()) {
            team.removeMembers(hero);
        }
        return hero;
    }

    public Team createNewTeam(String teamName, SuperHero... initialMembers) {
        Team newTeam = new Team();
        newTeam.setName(teamName);
        newTeam.addMembers(initialMembers);
        allTeams.put(teamName, newTeam);
        return newTeam;
    }

    public Team removeHeroesFromTeam(Team team, SuperHero... heroes) {
        team.removeMembers(heroes);
        for (SuperHero hero : heroes) {
            List<Team> teamAffiliations = hero.getTeamAffiliations();
            if (teamAffiliations != null) {
                teamAffiliations.remove(team);
            }
        }
        return team;
    }

    public Team removeHeroesFromTeam(Team team, Collection<SuperHero> heroes) {
        return removeHeroesFromTeam(team, heroes.toArray(new SuperHero[]{}));
    }

    public Team removeTeam(String teamName) throws UnknownTeamException {
        Team team = allTeams.remove(teamName);
        if (team == null) {
            throw new UnknownTeamException(teamName);
        }
        return removeHeroesFromTeam(team, allHeroes.values());
    }
}

