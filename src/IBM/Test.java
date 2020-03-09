//using System.Collections;
//using IBM.Cloud.SDK;
//using IBM.Cloud.SDK.Authentication.Iam;
//using IBM.Watson.Discovery.V1.Model;
//using IBM.Watson.Discovery.V1;
//using UnityEngine;
//
//
//namespace IBM
//{
//    public class Test
//    {
//        public void MyQuery()
//        {
//            //  Create credential and instantiate service
//            var authenticator = new IamAuthenticator(
//                apikey: "D9gnshMz8w-bCPcNDniglMnUODvf3ugukyHWWKVZmanP"
//            );
//
//            //  Wait for tokendata
//            while (!authenticator.CanAuthenticate())
//            {
//                Debug.Log("Authentication failed!");
//                return;
//            }
//
//            var discovery = new DiscoveryService("2020-02-27", authenticator);
//            discovery.SetServiceUrl("https://api.eu-gb.discovery.watson.cloud.ibm.com/instances/fb10cddb-3365-415f-be64-7af4ac2763f2");
//
//            QueryResponse queryResponse = null;
//            discovery.Query(
//                callback: (DetailedResponse<QueryResponse> response, IBMError error) =>
//                {
//                    Log.Debug("DiscoveryServiceV1", "Query result: {0}", response.Response);
//                    Debug.LogFormat("Query result: {0}", response.Response);
//                    queryResponse = response.Result;
//                },
//                environmentId: "system",
//                collectionId: "news-en"
//                // filter: "{filter}",
//                // query: "{query}",
//                // aggregation: "{aggregation}"
//            );
//
//            while (queryResponse == null)
//                return;
//
//        }
//
//
//    }
//}
