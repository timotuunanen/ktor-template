package com.tt.routes

import com.tt.models.BusinessId
import com.tt.models.Company
import com.tt.plugins.checkBasicAuthenticated
import com.tt.plugins.makeResponse
import com.tt.services.CompanyService
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.resources.delete
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.routing.routing
import mu.KotlinLogging
import org.koin.ktor.ext.inject

@Resource("/company")
class CompanyRoute(val businessId: BusinessId? = "") {

    @Resource("/by_status")
    class ByStatus(val parent: CompanyRoute = CompanyRoute(businessId = null), val status: Company.Status)

    @Resource("{id}")
    class Id(val parent: CompanyRoute = CompanyRoute(businessId = null), val id: String)
}

private val logger = KotlinLogging.logger {}

fun Application.companyRouting() {
    val companyService by inject<CompanyService>()

    routing {
        checkBasicAuthenticated {
            post<CompanyRoute> {
                call.makeResponse(
                    companyService.insertCompany(
                        call.receive<Company>()
                    ),
                    HttpStatusCode.Created
                )
            }
            delete<CompanyRoute.Id> {
                call.makeResponse(companyService.deleteCompany(it.id))
            }
            put<CompanyRoute> {
                call.makeResponse(
                    companyService.upsertCompany(call.receive<Company>()),
                    HttpStatusCode.Created
                )
            }
            get<CompanyRoute> {
                call.makeResponse(companyService.getCompany(it.businessId!!))
            }
            get<CompanyRoute.ByStatus> {
                call.makeResponse(companyService.getCompaniesByStatus(it.status))
            }
        }
    }
}
