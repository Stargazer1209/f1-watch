package com.f1watch

import com.f1watch.data.api.ApiClient
import com.f1watch.data.model.JolpicaRootResponse
import com.f1watch.data.model.RaceTableResponse
import com.f1watch.data.model.StandingsTableResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LiveApiDeserializationTest {

    private val json = ApiClient.json

    @Test
    fun testRaceTableDeserialization() {
        val jsonString = """
        {
          "MRData": {
            "xmlns": "http://ergast.com/mrd/1.5",
            "series": "f1",
            "url": "http://api.jolpi.ca/ergast/f1/current.json",
            "limit": "30",
            "offset": "0",
            "total": "23",
            "RaceTable": {
              "season": "2026",
              "Races": [
                {
                  "season": "2026",
                  "round": "1",
                  "url": "https://en.wikipedia.org/wiki/2026_Australian_Grand_Prix",
                  "raceName": "Australian Grand Prix",
                  "Circuit": {
                    "circuitId": "albert_park",
                    "circuitName": "Albert Park Grand Prix Circuit",
                    "Location": {
                      "lat": "-37.8497",
                      "long": "144.968",
                      "locality": "Melbourne",
                      "country": "Australia"
                    }
                  },
                  "date": "2026-03-08",
                  "time": "04:00:00Z",
                  "FirstPractice": { "date": "2026-03-06", "time": "01:30:00Z" },
                  "SecondPractice": { "date": "2026-03-06", "time": "05:00:00Z" },
                  "ThirdPractice": { "date": "2026-03-07", "time": "01:30:00Z" },
                  "Qualifying": { "date": "2026-03-07", "time": "05:00:00Z" }
                },
                {
                  "season": "2026",
                  "round": "2",
                  "raceName": "Chinese Grand Prix",
                  "Circuit": {
                    "circuitId": "shanghai",
                    "circuitName": "Shanghai International Circuit",
                    "Location": {
                      "lat": "31.3389",
                      "long": "121.22",
                      "locality": "Shanghai",
                      "country": "China"
                    }
                  },
                  "date": "2026-03-15",
                  "time": "07:00:00Z",
                  "FirstPractice": { "date": "2026-03-13", "time": "03:30:00Z" },
                  "SprintQualifying": { "date": "2026-03-13", "time": "07:30:00Z" },
                  "Sprint": { "date": "2026-03-14", "time": "03:00:00Z" },
                  "Qualifying": { "date": "2026-03-14", "time": "07:00:00Z" }
                }
              ]
            }
          }
        }
        """.trimIndent()

        val root = json.decodeFromString<JolpicaRootResponse<RaceTableResponse>>(jsonString)
        val races = root.mrData.raceTable.races
        assertEquals(2, races.size)
        assertEquals("1", races[0].round)
        assertEquals("Australian Grand Prix", races[0].raceName)
        assertEquals("2", races[1].round)
        assertNotNull(races[1].sprint)
        assertNotNull(races[1].sprintQualifying)

        val domainRace = races[1].toDomainRace()
        assertEquals("🇨🇳", domainRace.circuit.flagEmoji)
        assertEquals(5, domainRace.getAllSessions().size)
    }

    @Test
    fun testRaceResultsDeserialization() {
        val jsonString = """
        {
          "MRData": {
            "RaceTable": {
              "season": "2026",
              "round": "12",
              "Races": [
                {
                  "season": "2026",
                  "round": "12",
                  "raceName": "Dutch Grand Prix",
                  "Circuit": {
                    "circuitId": "zandvoort",
                    "circuitName": "Circuit Park Zandvoort",
                    "Location": {
                      "lat": "52.3888",
                      "long": "4.54092",
                      "locality": "Zandvoort",
                      "country": "Netherlands"
                    }
                  },
                  "date": "2026-08-23",
                  "time": "13:00:00Z",
                  "Results": [
                    {
                      "number": "4",
                      "position": "1",
                      "positionText": "1",
                      "points": "25",
                      "Driver": {
                        "driverId": "norris",
                        "permanentNumber": "4",
                        "code": "NOR",
                        "givenName": "Lando",
                        "familyName": "Norris"
                      },
                      "Constructor": {
                        "constructorId": "mclaren",
                        "name": "McLaren",
                        "nationality": "British"
                      },
                      "grid": "1",
                      "laps": "72",
                      "status": "Finished",
                      "Time": { "millis": "4986801", "time": "1:23:06.801" },
                      "FastestLap": { "rank": "1", "lap": "72", "Time": { "time": "1:13.817" } }
                    }
                  ]
                }
              ]
            }
          }
        }
        """.trimIndent()

        val root = json.decodeFromString<JolpicaRootResponse<RaceTableResponse>>(jsonString)
        val results = root.mrData.raceTable.races[0].results
        assertEquals(1, results.size)
        assertEquals("NOR", results[0].driver.code)
        assertEquals("1", results[0].fastestLap?.rank)
    }

    @Test
    fun testStandingsDeserialization() {
        val jsonString = """
        {
          "MRData": {
            "StandingsTable": {
              "season": "2026",
              "round": "12",
              "StandingsLists": [
                {
                  "season": "2026",
                  "round": "12",
                  "DriverStandings": [
                    {
                      "position": "1",
                      "positionText": "1",
                      "points": "242",
                      "wins": "6",
                      "Driver": {
                        "driverId": "antonelli",
                        "permanentNumber": "12",
                        "code": "ANT",
                        "givenName": "Andrea Kimi",
                        "familyName": "Antonelli"
                      },
                      "Constructors": [
                        { "constructorId": "mercedes", "name": "Mercedes" }
                      ]
                    }
                  ]
                }
              ]
            }
          }
        }
        """.trimIndent()

        val root = json.decodeFromString<JolpicaRootResponse<StandingsTableResponse>>(jsonString)
        val driverStandings = root.mrData.standingsTable.standingsLists[0].driverStandings
        assertEquals(1, driverStandings.size)
        assertEquals("ANT", driverStandings[0].driver.code)
        assertEquals("242", driverStandings[0].points)
    }
}
