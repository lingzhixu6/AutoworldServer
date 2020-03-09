//using System.Collections;
//using System.Collections.Generic;
//using UnityEngine;
//using IBM.Cloud.SDK;
//using IBM.Cloud.SDK.Authentication;
//using IBM.Cloud.SDK.Authentication.Bearer;
//using IBM.Cloud.SDK.Authentication.Iam;
//using IBM.Watson.Assistant.V1;
//using IBM.Watson.Assistant.V1.Model;
//using IBM.Watson.Discovery.V1.Model;
//using IBM.Watson.Discovery.V1;
//using UnityEngine;
//
//public class Test2
//{
//    Authenticator authenticator;
//    AssistantService assistant;
//    string versionDate = "2020-03-03";
//
//    public void TokenExample()
//    {
//        //  Create authenticator using the Bearer Token
//        authenticator = new BearerTokenAuthenticator(
//            "eyJraWQiOiIyMDIwMDIyNTE4MjgiLCJhbGciOiJSUzI1NiJ9.eyJpYW1faWQiOiJpYW0tU2VydmljZUlkLWIwN2VmMThjLTM2N2ItNDBkYS05MGU3LWNlMDNmZDAyM2YxMyIsImlkIjoiaWFtLVNlcnZpY2VJZC1iMDdlZjE4Yy0zNjdiLTQwZGEtOTBlNy1jZTAzZmQwMjNmMTMiLCJyZWFsbWlkIjoiaWFtIiwiaWRlbnRpZmllciI6IlNlcnZpY2VJZC1iMDdlZjE4Yy0zNjdiLTQwZGEtOTBlNy1jZTAzZmQwMjNmMTMiLCJzdWIiOiJTZXJ2aWNlSWQtYjA3ZWYxOGMtMzY3Yi00MGRhLTkwZTctY2UwM2ZkMDIzZjEzIiwic3ViX3R5cGUiOiJTZXJ2aWNlSWQiLCJhY2NvdW50Ijp7InZhbGlkIjp0cnVlLCJic3MiOiJlNDA0ZTg0MmEyZTQ0YTAyOGVkYWFmNTQxOTE3NzZkMCJ9LCJpYXQiOjE1ODMyNjc5MjgsImV4cCI6MTU4MzI3MTUyOCwiaXNzIjoiaHR0cHM6Ly9pYW0uY2xvdWQuaWJtLmNvbS9pZGVudGl0eSIsImdyYW50X3R5cGUiOiJ1cm46aWJtOnBhcmFtczpvYXV0aDpncmFudC10eXBlOmFwaWtleSIsInNjb3BlIjoiaWJtIG9wZW5pZCIsImNsaWVudF9pZCI6ImRlZmF1bHQiLCJhY3IiOjEsImFtciI6WyJwd2QiXX0.DgZg2ERvw1HPEMYSfiAk0lT1vJyn99_V_n13mVCIYveilg2LwoMenpG-lC6FzdH8ml_CuOLsAzW5DRiKdSuKzznbkxGw_B1RGFLedhRLdARbS45DBSjUDPhJ89BXUZuudEniOSmHD0L7X-Wj_ePR3VeyzK4NIW5-pDQFTUAkrTh6swpkpBbm6PL2GfTdm1Kxdz0Wzc8JvCFJ0R1Yq7ln05P4AUeHnmpXFOToEY0mkFPx1oR6DBX7yqpwhoWUpx-3XiMDdNF8vnThWp68_LminSLh6cIYu_hd57umSrFdJAbIBjZNHhvliwRZVIlzfecyKH-jkmGKlDNI5CFBJVOXpg");
//
//        var discovery = new DiscoveryService("2020-02-27", authenticator);
//        discovery.SetServiceUrl("https://api.eu-gb.discovery.watson.cloud.ibm.com/instances/fb10cddb-3365-415f-be64-7af4ac2763f2");
//
//        QueryResponse queryResponse = null;
//        discovery.Query(
//            callback: (DetailedResponse<QueryResponse> response, IBMError error) =>
//            {
//                Log.Debug("DiscoveryServiceV1", "Query result: {0}", response.Response);
//                Debug.LogFormat("Query result: {0}", response.Response);
//                queryResponse = response.Result;
//            },
//            environmentId: "system",
//            collectionId: "news-en"
//            // filter: "{filter}",
//            // query: "{query}",
//            // aggregation: "{aggregation}"
//        );
//
//        while (queryResponse == null)
//            return;
//
//    }
//
//    // public void TokenExample2()
//    // {
//    //     public IEnumerator ExampleAutoService()
//    //     {
//    //         Assistant assistantService = new Assistant("2019-04-03");
//    //
//    //         //  Wait for authorization token
//    //         while (!assistantService.Authenticator.CanAuthenticate())
//    //             yield return null;
//    //
//    //         var listWorkspacesResult = assistantService.ListWorkspaces();
//    //     }
//    // }
//
//
//}
