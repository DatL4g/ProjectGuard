// The report data is injected by the report generator
document.addEventListener('DOMContentLoaded', function() {
    if (window.REPORT_DATA) {
        renderSidebar(window.REPORT_DATA);
        renderMainContent(window.REPORT_DATA);
    } else {
        document.body.innerHTML = "Failed to load report data.";
    }
});

function getGroupName(modulePath) {
    const parts = modulePath.split(':');
    if (parts.length > 2) {
        return `:${parts[1]}`;
    }
    return "Top-level Modules";
}

function renderSidebar(report) {
    const sidebar = document.getElementById('sidebar');
    const fatalModules = report.modules.filter(m => m.fatal.length > 0);
    const suppressedModules = report.modules.filter(m => m.suppressed.length > 0);
    sidebar.innerHTML = `
        ${generateToc(fatalModules, 'Fatal Matches', false)}
        ${generateToc(suppressedModules, 'Suppressed Matches', true)}
    `;
}

function generateToc(modules, title, isSuppressed) {
    if (modules.length === 0) return '';
    const anchorPrefix = isSuppressed ? 'suppressed' : 'fatal';
    const groups = modules.reduce((acc, module) => {
        const groupName = getGroupName(module.module);
        if (!acc[groupName]) {
            acc[groupName] = [];
        }
        acc[groupName].push(module);
        return acc;
    }, {});

    const tocHtml = Object.keys(groups).sort().map(groupName => {
        const groupModules = groups[groupName];
        const count = groupModules.reduce((sum, m) => sum + (isSuppressed ? m.suppressed.length : m.fatal.length), 0);
        const badgeClass = isSuppressed ? 'suppressed' : 'fatal';
        const groupAnchorId = `${anchorPrefix}-${groupName.replace(/:/g, "").replace(/ /g, "-").toLowerCase()}`;

        const childModulesHtml = groupModules.sort((a,b) => a.module.localeCompare(b.module)).map(moduleReport => {
            const childCount = isSuppressed ? moduleReport.suppressed.length : moduleReport.fatal.length;
            if (childCount === 0) return '';
            const moduleAnchorId = `${anchorPrefix}-module-${moduleReport.module.substring(1).replace(/:/g, "-")}`;
            const childBadgeClass = isSuppressed ? 'suppressed-light' : 'fatal-light';
            const moduleName = moduleReport.module.split(':').pop();
            return `
                <li>
                    <a href="#${moduleAnchorId}">
                        <span>${moduleName}</span>
                        <span class="badge ${childBadgeClass}">${childCount}</span>
                    </a>
                </li>
            `;
        }).join('');

        return `
            <details>
                <summary>
                     <a href="#${groupAnchorId}">${groupName}</a>
                     <span class="badge ${badgeClass}">${count}</span>
                </summary>
                ${childModulesHtml ? `<ul>${childModulesHtml}</ul>` : ''}
            </details>
        `;
    }).join('');

    return `
        <div class="toc-section">
            <h2>${title}</h2>
            <nav>${tocHtml}</nav>
        </div>
    `;
}

function renderMainContent(report) {
    const mainContent = document.getElementById('main-content');
    const fatalModules = report.modules.filter(m => m.fatal.length > 0);
    const suppressedModules = report.modules.filter(m => m.suppressed.length > 0);
    const totalFatalMatches = fatalModules.reduce((sum, m) => sum + m.fatal.length, 0);

    mainContent.innerHTML = `
        <div class="container">
            <header>
                <h1>DependencyGuard Report</h1>
                <p style="color: var(--color-text-secondary); margin: 0.25rem 0 0 0;">
                    Found ${totalFatalMatches} fatal matches across ${report.modules.length} modules.
                </p>
            </header>
            <main>
                ${generateSectionContent("Fatal Matches", fatalModules, false)}
                ${generateSectionContent("Suppressed Matches", suppressedModules, true)}
            </main>
        </div>
    `;
}

function generateSectionContent(title, modules, isSuppressed) {
    if (modules.length === 0) return '';
    const groups = modules.reduce((acc, module) => {
        const groupName = getGroupName(module.module);
        if (!acc[groupName]) {
            acc[groupName] = [];
        }
        acc[groupName].push(module);
        return acc;
    }, {});

    const groupHtml = Object.keys(groups).sort().map(groupName => {
        return generateModuleGroup(groupName, groups[groupName], isSuppressed);
    }).join('');

    const titleClass = isSuppressed ? "suppressed-title" : "fatal-title";
    return `
        <section>
            <h2 class="${titleClass}">${title}</h2>
            ${groupHtml}
        </section>
    `;
}

function generateModuleGroup(groupName, modules, isSuppressed) {
    const anchorPrefix = isSuppressed ? 'suppressed' : 'fatal';
    const anchorId = `${anchorPrefix}-${groupName.replace(/:/g, "").replace(/ /g, "-").toLowerCase()}`;
    const moduleDetailsHtml = modules.map(moduleReport => {
        return generateModuleDetails(moduleReport, isSuppressed);
    }).join('');

    return `
        <div class="group-container" id="${anchorId}">
            <h3>${groupName}</h3>
            ${moduleDetailsHtml}
        </div>
    `;
}

function generateModuleDetails(moduleReport, isSuppressed) {
    const anchorPrefix = isSuppressed ? 'suppressed' : 'fatal';
    const moduleAnchorId = `${anchorPrefix}-module-${moduleReport.module.substring(1).replace(/:/g, "-")}`;
    const table = isSuppressed
        ? generateSuppressedTable(moduleReport.suppressed)
        : generateFatalTable(moduleReport.fatal);

    return `
        <details class="module" id="${moduleAnchorId}" open>
            <summary>
                <h4>${moduleReport.module}</h4>
                <div>
                    ${moduleReport.fatal.length > 0 ? `<span class="badge fatal">${moduleReport.fatal.length} fatal</span>` : ''}
                    ${moduleReport.suppressed.length > 0 ? `<span class="badge suppressed">${moduleReport.suppressed.length} suppressed</span>` : ''}
                </div>
            </summary>
            ${table}
        </details>
    `;
}

function generateFatalTable(matches) {
    if (!matches || matches.length === 0) return '';
    const tableRows = matches.map(match => `
        <tr>
            <td><code>${match.pathToDependency}</code></td>
            <td>${match.reason}</td>
        </tr>
    `).join('');
    return `
        <div class="table-container">
            <table>
                <thead>
                    <tr>
                        <th>Dependency</th>
                        <th>Restriction Reason</th>
                    </tr>
                </thead>
                <tbody>${tableRows}</tbody>
            </table>
        </div>
    `;
}

function generateSuppressedTable(matches) {
    if (!matches || matches.length === 0) return '';
    const tableRows = matches.map(match => `
        <tr>
            <td><code>${match.pathToDependency}</code></td>
            <td>${match.suppressionReason}</td>
        </tr>
    `).join('');
    return `
        <div class="table-container">
            <table>
                <thead>
                    <tr>
                        <th>Dependency</th>
                        <th>Suppression Reason</th>
                    </tr>
                </thead>
                <tbody>${tableRows}</tbody>
            </table>
        </div>
    `;
}
