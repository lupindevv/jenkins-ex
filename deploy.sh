#!/bin/bash
                            # Pull the latest image
                            docker pull alexthm1/demo-app:11.0.0-16
                            
                            # Stop and remove existing container if it exists
                            docker stop demo-app || true
                            docker rm demo-app || true
                            
                            # Run the new container
                            docker run -d \
                                --name demo-app \
                                -p 3000:3000 \
                                --restart unless-stopped \
                                alexthm1/demo-app:11.0.0-16
                                
                            # Display container status
                            docker ps | grep demo-app
                        